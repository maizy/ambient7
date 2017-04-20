package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.nio.file.{ FileSystems, Path }
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

case object Defaults {
  val INFLUXDB_BASEURL = "http://localhost:8086/"
  val INFLUXDB_AGENT_NAME = "main"
  val DB_USER = "ambient7"
  val DB_PASSWORD = ""
  val DB_URL = "jdbc:h2:file:/var/ambient7/analysis;AUTO_SERVER=TRUE"
  val UNIVERSAL_CONFIG_PATH: Path = FileSystems.getDefault.getPath("/etc", "ambient7", "application.conf")
  val NOTIFICATION_REFRESH_RATE = Duration(1, TimeUnit.MINUTES)
}
