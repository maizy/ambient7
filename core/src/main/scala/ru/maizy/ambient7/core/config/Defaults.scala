package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case object Defaults {
  val INFLUXDB_BASEURL = "http://localhost:8086/"
  val INFLUXDB_AGENT_NAME = "main"
  val DB_USER = "ambient7"
  val DB_PASSWORD = "ambient7"
  val DB_URL = "jdbc:h2:file:/var/ambient7/analysis;AUTO_SERVER=TRUE"
}
