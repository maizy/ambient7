package ru.maizy.ambient7.analysis

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.DurationInt
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.influxdbclient.InfluxDbClient


package object influxdb {
  def buildClient(opts: Ambient7Options): Option[InfluxDbClient] = {
    (
      opts.influxDb.flatMap(_.clientConnectionSettings),
      opts.influxDb.flatMap(_.readonlyClientConnectionSetting)
    ) match {
      case (Some(setting), Some(readonlySettings)) =>
        Some(
          new InfluxDbClient(
            influxDbSettings = setting,
            _influxDbReadonlySettings = Some(readonlySettings),
            userAgent = Some("ambient7-analysis"),
            connectTimeout = 500.millis,
            readTimeout = 10.seconds
          )
        )
      case _ => None
    }
  }
}
