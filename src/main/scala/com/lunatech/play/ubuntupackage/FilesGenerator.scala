package com.lunatech.play.ubuntupackage

object FilesGenerator {

  /*
   * Default respawn limit is 10 times in 5 seconds. So if failing to start takes longer than 0.5 seconds
   * it will never hit this limit. JVM startup time is probably that long. So we're taking a smaller number of
   * tries and longer timeout.
   */
  def upstartScript(config: ApplicationConfiguration) =
    """|start on runlevel [2345]
       |stop on runlevel [016]
       |respawn
       |respawn limit 3 60
       |console log
       |setuid %s
       |setgid %s
       |env PLAY_OPTS="-Dhttp.port=%s -Dconfig.file=/etc/%s/custom.conf"
       |pre-start exec rm -f %s
       |exec %s $PLAY_OPTS""".stripMargin.format(config.user, config.group, config.port, config.name, config.dir + "/RUNNING_PID", config.dir + "/start")

  def preInstall(config: ApplicationConfiguration) = Some(
    """|#!/bin/sh
       |# Create the request user and group if they don't exist yet.
       |set -e
       |if ! getent group|grep ^%s: >/dev/null 2>&1 ; then
       |  addgroup %s %s
       |fi
       |if ! id -u %s >/dev/null 2>&1 ; then
       |  adduser %s %s
       |fi
       |""".stripMargin.format(
         config.group,
         config.addGroupOptions mkString " ",
         config.group,
         config.user,
         config.addUserOptions mkString " ",
         config.user))

  /*
   * Setting ownership on a directory doesn't work with Native Packager Plugin 0.4.4, so we do it
   * in the postinst script.
   *
   * service restart gives an error if the package is not running (new install) and service start
   * gives and error if the service is running (upgrade). So we stop first, and then restart.
   */
  def postInstall(config: ApplicationConfiguration) = Some(
    """|#!/bin/sh
       |chown %s %s
       |
       |if service %s status|grep running ; then
       |  service %s restart
       |else
       |  service %s start
       |fi
       |""".stripMargin.format(config.user, config.dir, config.name, config.name, config.name))

  def preRemoval(config: ApplicationConfiguration) = Some(
    """|#!/bin/sh
       |
       |if service %s status|grep running ; then
       |  service %s stop
       |fi
       |""".stripMargin.format(config.name, config.name))

  /*
   *  We don't know whether you created a user for this service, or used an existing
   *  user, so we're not going to remove it for now...
   */
  def postRemoval(config: ApplicationConfiguration) = None

  def configFile(config: ApplicationConfiguration) =
    """|# Include the base configuration from the application's classpath
       |include "application"
       |
       |# Put your own overrides below this line
       |
       |""".stripMargin
}