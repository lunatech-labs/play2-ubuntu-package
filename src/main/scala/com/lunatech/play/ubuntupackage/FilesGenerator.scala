package com.lunatech.play.ubuntupackage

object FilesGenerator {
  
  def upstartScript(config: ApplicationConfiguration) = 
    """|start on runlevel [2345]
       |stop on runlevel [016]
       |respawn
       |console log
       |setuid %s
       |setgid %s
       |env PLAY_OPTS=-Dhttp.port=%s
       |exec %s $PLAY_OPTS""".stripMargin.format(config.user, config.group, config.port, config.dir + "/start")

  def preInstall(config: ApplicationConfiguration) = Some(
    """|#!/bin/sh
       |# preinst script for %1$s
       |set -e
       |addgroup --system %s
       |adduser --system --no-create-home --disabled-password --shell /bin/false %s""".stripMargin.format(config.group, config.user))
      
  /*
   * Setting ownership on a directory doesn't work with Native Packager Plugin 0.4.4, so we do it 
   * in the postinst scrip.
   */
  def postInstall(config: ApplicationConfiguration) = Some(
    """|#!/bin/sh
       |chown %s %s
       |service %s start""".stripMargin.format(config.user, config.dir, config.name))
  
  def preRemoval(config: ApplicationConfiguration) = Some(
    """|#!/bin/sh
       |service %s stop || true""".stripMargin.format(config.name))

  /*
   *  We don't know whether you created a user for this service, or used an existing
   *  user, so we're not going to remove it for now...
   */
  def postRemoval(config: ApplicationConfiguration) = None

}