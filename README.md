Play 2 Ubuntu Upstart packager plugin
=====================================

This plugin adds tasks to create a .deb package for an application. It is built on top of [sbt-native-packager](https://github.com/sbt/sbt-native-packager) The generated package uses Upstart as process manager, as opposed to old-style init scripts with start-stop-daemon. A modern Ubuntu (12.04 or newer) is needed to use these packages. This package is heavily inspired by [play2-native-packager-plugin](https://github.com/kryptt/play2-native-packager-plugin). If you prefer more widely usable Debian packages, please use that project.

Installation
------------

In your `project/plugins.sbt` file, add the following:

    resolvers += Resolver.url("Lunatech SBT Plugins", new URL("http://artifactory.lunatech.com/artifactory/sbt-plugins-public"))(Resolver.ivyStylePatterns)

    resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.lunatech" % "play2-ubuntu-package" % "0.3")

Versions
--------

* 0.3 is for Play 2.0.4
* 0.4 is for Play 2.1-RC3

Usage
-----

In `project/Build.scala`, import the following:

    import com.lunatech.play.ubuntupackage.UbuntuPackagePlugin._

Then, add the `ubuntuPackageSettings` to your project:

    settings(ubuntuPackageSettings:_*).settings( // your other settings

Now, there are various TaskKeys you can set from `UbuntuPackageKeys`. Only one is required, `maintainer`:

    UbuntuPackageKeys.maintainer := "Erik Bakker <erik@lunatech.com>"

To build the package use the `deb` task.

Developing
----------

If you want to work on this plugin, check it out somewhere and include it in the build of the project you're trying to build. This means, create in your app file `project/project/Build.scala` with the following contents:

    import sbt._
    object PluginDef extends Build {
      override lazy val projects = Seq(root)
      lazy val root = Project("plugins", file(".")) dependsOn(webPlugin)
      lazy val webPlugin = file("/Users/eamelink/projects/play2-ubuntu-package")
    }

