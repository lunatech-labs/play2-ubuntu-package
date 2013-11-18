resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns))

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.0")

addSbtPlugin("play" % "sbt-plugin" % "2.1.2")

sbtPlugin := true

name := "play2-ubuntu-package"

organization := "com.lunatech"

version := "0.7"

description := "Play 2 plugin for building Ubuntu packages"

publishMavenStyle := false

publishArtifact in Test := false

publishTo := Some(Resolver.url("sbt-plugins-public", new URL("http://artifactory.lunatech.com/artifactory/sbt-plugins-public/"))(Resolver.ivyStylePatterns))
