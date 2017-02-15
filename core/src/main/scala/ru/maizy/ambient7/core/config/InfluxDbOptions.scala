package ru.maizy.ambient7.core.config

import ru.maizy.influxdbclient.InfluxDbConnectionSettings

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
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
) {

  lazy val clientConnectionSettings: Option[InfluxDbConnectionSettings] =
    database.map(
      InfluxDbConnectionSettings(
        baseUrl,
        _,
        user,
        password
      )
    )

  lazy val readonlyClientConnectionSetting: Option[InfluxDbConnectionSettings] =
    database.map(
      InfluxDbConnectionSettings(
        readonlyBaseUrl.getOrElse(baseUrl),
        _,
        readonlyUser,
        readonlyPassword
      )
    )
}
