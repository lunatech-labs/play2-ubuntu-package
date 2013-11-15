package com.lunatech.play.ubuntupackage

import com.typesafe.sbt.packager
import com.typesafe.sbt.packager._
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.{ Keys => DebianKeys }
import sbt._
import sbt.Keys._

object UbuntuPackagePlugin extends Plugin with DebianPlugin {
  object UbuntuPackageKeys {
    def user = SettingKey[String]("ubuntu-user", "Ubuntu system user for this application")
    def group = SettingKey[String]("ubuntu-group", "Ubuntu system group for this application")
    def installationDirectory = TaskKey[String]("ubuntu-installation-directory", "Installation dir for this application")
    def port = SettingKey[Int]("ubuntu-default-port", "Default Port")
    def applicationConfiguration = TaskKey[ApplicationConfiguration]("ubuntu-application-configuration", "Application configuration")
    def maintainer = packager.Keys.maintainer
    def adduserOptions = SettingKey[Seq[String]]("ubuntu-adduser-options", "Additional options for the 'adduser' command")
    def addgroupOptions = SettingKey[Seq[String]]("ubuntu-addgroup-options", "Additional options for the 'addgroup' command")
    def systemProperties = SettingKey[Map[String, String]]("ubuntu-system-properties", "Additional system properties to be set on startup")

    def deb = TaskKey[File]("deb", "Build the 'deb' package")
    // TODO: It's nicer to have these tasks just generate tuples of name/content/perms/user/group, and then make a sequence of them in a single task.
    def upstartConfig = TaskKey[File]("ubuntu-upstart-config", "Create the Ubuntu upstart config file")
    def configFile = TaskKey[File]("ubuntu-config-file", "Create the application configuration file")
    def preInstall = TaskKey[Option[File]]("ubuntu-pre-install", "Create the Ubuntu preinst file")
    def postInstall = TaskKey[Option[File]]("ubuntu-post-install", "Create the Ubuntu postinst file")
    def preRemoval = TaskKey[Option[File]]("ubuntu-pre-removal", "Create the Ubuntu prerm file")
    def postRemoval = TaskKey[Option[File]]("ubuntu-post-removal", "Create the Ubuntu postrm file")
  }

  import UbuntuPackageKeys._

  lazy val ubuntuPackageSettings: Seq[Project.Setting[_]] = linuxSettings ++ debianSettings ++ Seq(
    name in Debian <<= normalizedName,
    version in Debian <<= version,
    user <<= normalizedName,
    group <<= user,
    adduserOptions := Seq("--system", "--no-create-home", "--disabled-password", "--disabled-login"),
    addgroupOptions := Seq("--system"),
    port := 9000,
    DebianKeys.packageDescription <<= description,
    DebianKeys.packageSummary <<= description,
    installationDirectory <<= (name in Debian) map ("/opt/" + _),
    systemProperties := Map(),

    DebianKeys.debianPackageDependencies in Debian ++= Seq("java2-runtime", "upstart (>= 1.5)"),

    applicationConfiguration <<= (name in Debian, user, group, installationDirectory, port, adduserOptions, addgroupOptions, systemProperties) map {
      ApplicationConfiguration(_, _, _, _, _, _, _, _)
    },

    DebianKeys.linuxPackageMappings <++=
      (baseDirectory, target, applicationConfiguration, DebianKeys.packageSummary, PlayProject.dist, upstartConfig, configFile) map {
      (baseDir, targetDir, appConfig, descriptionValue, distZip, upstartConfig, configFile) =>
        val applicationDir = "opt/%s" format appConfig.name
        val distDir = targetDir / "dist-zip"
        IO.delete(distDir)
        IO.unzip(distZip, distDir)

        val unpackedAppDir: File = (distDir * new FileFilter { def accept(f: File) = f.isDirectory }).get.head

        Seq(
          packageMapping(unpackedAppDir -> applicationDir) withUser(appConfig.user) withGroup(appConfig.group) withPerms("0777"),
          packageMapping(upstartConfig -> "/etc/init/%s.conf".format(appConfig.name)) withPerms("0644") withConfig(),
          packageMapping(configFile -> "/etc/%s/custom.conf".format(appConfig.name)) withPerms("0644") withConfig()
        ) ++ (for {
          path <- (unpackedAppDir ***).get
          if !path.isDirectory
        } yield {
          val mapping = packageMapping(path -> path.toString.replaceFirst(unpackedAppDir.toString, applicationDir)) withUser(appConfig.user) withGroup(appConfig.group) withPerms("0644")
          if(path.toString.endsWith("start")) {
            mapping withPerms("0755")
          } else {
            mapping
          }
        })

    },

    deb <<= packageBin in Debian,

    upstartConfig <<= (target, applicationConfiguration) map { (dir, config) =>
      val file = dir / "%s.upstart".format(config.name)
      IO.write(file, FilesGenerator.upstartScript(config))
      file
    },
    configFile <<= (target, applicationConfiguration) map { (dir, config) =>
      val file = dir / "custom.conf"
      IO.write(file, FilesGenerator.configFile(config))
      file
    },
    preInstall <<= (target in Debian, applicationConfiguration) map { (dir, config) =>
      writeDebianScript(dir / "DEBIAN" / "preinst", FilesGenerator.preInstall(config))
    },
    postInstall <<= (target in Debian, applicationConfiguration) map { (dir, config) =>
      writeDebianScript(dir / "DEBIAN" / "postinst", FilesGenerator.postInstall(config))
    },
    preRemoval <<= (target in Debian, applicationConfiguration) map { (dir, config) =>
      writeDebianScript(dir / "DEBIAN" / "prerm", FilesGenerator.preRemoval(config))
    },
    postRemoval <<= (target in Debian, applicationConfiguration) map { (dir, config) =>
      writeDebianScript(dir / "DEBIAN" / "postrm", FilesGenerator.postRemoval(config))
    },
    (DebianKeys.debianExplodedPackage in Debian) <<= (DebianKeys.debianExplodedPackage in Debian) dependsOn (preInstall, postInstall, preRemoval, postRemoval)

  ) ++
  SettingsHelper.makeDeploymentSettings(Debian, packageBin in Debian, "deb")

  def writeDebianScript(file: File, content: Option[String]) = {
    content.map { content =>
      IO.write(file, content)
      Seq("chmod", "+x", file.getAbsolutePath).!
      file
    }
  }
}
