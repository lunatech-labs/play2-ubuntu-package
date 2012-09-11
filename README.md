Play 2 Ubuntu Upstart packager plugin
=====================================

This plugin adds tasks to create a .deb package for an application. It is built on top of [sbt-native-packager](https://github.com/sbt/sbt-native-packager) The generated package uses Upstart as process manager, as opposed to old-style init scripts with start-stop-daemon. A modern Ubuntu (12.04 or newer) is needed to use these packages. This package is heavily inspired by [play2-native-packager-plugin](https://github.com/kryptt/play2-native-packager-plugin). If you prefer more widely usable Debian packages, please use that project.

Installation
------------

In your `project/plugins.sbt` file, add the following:

    resolvers += Resolver.url("Lunatech SBT Plugins", new URL("http://artifactory.lunatech.com/artifactory/sbt-plugins-public"))(Resolver.ivyStylePatterns)

    addSbtPlugin("com.lunatech" % "play2-ubuntu-package" % "0.1")

Usage
-----

Import the following:

    import com.lunatech.play.ubuntupackage.UbuntuPackagePlugin._

Now, there are various TaskKeys you can set from `UbuntuPackageKeys`. Only one is required, `maintainer`:

    UbuntuPackageKeys.maintainer := "Erik Bakker <erik@lunatech.com>"

To build the package use the `deb` task.