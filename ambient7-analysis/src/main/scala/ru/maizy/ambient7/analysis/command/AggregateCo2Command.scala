package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.DurationInt
import ru.maizy.influxdbclient.{ InfluxDbClient, InfluxDbConnectionSettings }
import ru.maizy.ambient7.analysis.AppOptions

object AggregateCo2Command {
  def run(opts: AppOptions): ReturnStatus = {
    if (opts.influxDbDatabase.isEmpty || opts.influxDbReadonlyBaseUrl.isEmpty) {
      ReturnStatus(2)
    } else {
      val influxDbDatabase = opts.influxDbDatabase.get
      val writableSettings = InfluxDbConnectionSettings(
        opts.influxDbBaseUrl,
        influxDbDatabase,
        opts.influxDbUser,
        opts.influxDbPassword
      )

      val readonlySettings = InfluxDbConnectionSettings(
        opts.influxDbReadonlyBaseUrl.get,
        influxDbDatabase,
        opts.influxDbReadonlyUser,
        opts.influxDbReadonlyPassword
      )

      val influxDbClient = new InfluxDbClient(
        influxDbSettings = writableSettings,
        _influxDbReadonlySettings = Some(readonlySettings),
        userAgent = Some("ambient7-analysis"),
        connectTimeout = 500.millis,
        readTimeout = 10.seconds
      )

      def printRes(q: String) {
        val res = influxDbClient.rawDataQuery(q)
        val utf8Res = new String(res, "utf-8")
        println(utf8Res)
      }

      printRes("make an error")
      printRes("show measurements")
      printRes(
        "select max(ppm) from co2 " +
          "where time >= '2015-11-06 00:00:00' and time < '2015-11-06 01:00:00' " +
          "and agent = 'main' group by time(1m)"
      )


      ReturnStatus.success
    }
  }
}
