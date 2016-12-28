package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class InfluxDbOptions(
    database: Option[String] = None,

    baseUrl: String = Defaults.INFLUXDB_BASEURL,
    user: Option[String] = None,
    password: Option[String] = None,

    readonlyBaseUrl: Option[String] = None,
    readonlyUser: Option[String] = None,
    readonlyPassword: Option[String] = None
)
