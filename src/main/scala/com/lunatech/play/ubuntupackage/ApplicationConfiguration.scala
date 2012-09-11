package com.lunatech.play.ubuntupackage

case class ApplicationConfiguration(
  name: String,
  user: String,
  group: String,
  dir: String,
  port: Int)