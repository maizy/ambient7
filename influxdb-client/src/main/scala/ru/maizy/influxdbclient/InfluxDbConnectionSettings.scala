package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

case class InfluxDbConnectionSettings(
    baseUrl: String,
    db: String,
    user: Option[String],
    password: Option[String]
)
