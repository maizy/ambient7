package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class InfluxDbOptions(
    baseUrl: String = Defaults.INFLUXDB_BASEURL,
    database: Option[String] = None,
    user: Option[String] = None,
    password: Option[String] = None,
    agentName: String = Defaults.INFLUXDB_AGENT_NAME,
    tags: String = ""
)
