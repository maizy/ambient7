package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.temporal.ChronoUnit
import java.time.{ ZoneId, ZoneOffset, ZonedDateTime }
import scala.concurrent.duration.DurationInt
import ru.maizy.influxdbclient.{ InfluxDbClient, InfluxDbConnectionSettings, Tags }
import ru.maizy.ambient7.analysis.AppOptions
import ru.maizy.ambient7.analysis.aggregate.Co2LevelsAnalysis

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

      println(
        Co2LevelsAnalysis.compute(
          influxDbClient,
          from = ZonedDateTime.of(2015, 11, 5, 0, 0, 0, 0, ZoneId.systemDefault()),
          until = ZonedDateTime.of(2015, 11, 6, 0, 0, 0, 0, ZoneId.systemDefault()),
          agentName = "main",
          tags = Tags()
        )
      )

      println(
        Co2LevelsAnalysis.compute(
          influxDbClient,
          from = ZonedDateTime.of(2015, 11, 7, 0, 0, 0, 0, ZoneId.systemDefault()),
          until = ZonedDateTime.of(2017, 11, 8, 0, 0, 0, 0, ZoneId.systemDefault()),
          agentName = "main",
          tags = Tags()
        )
      )

      println(
        Co2LevelsAnalysis.compute(
          influxDbClient,
          from = ZonedDateTime.of(2017, 1, 2, 0, 0, 0, 0, ZoneId.systemDefault()),
          until = ZonedDateTime.of(2017, 1, 3, 0, 0, 0, 0, ZoneId.systemDefault()),
          agentName = "main",
          tags = Tags()
        )
      )

      println("start date from now")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = ZonedDateTime.now(),
          agentName = "main",
          tags = Tags()
        )
      )

      val upperBoundForTestDataset = ZonedDateTime.of(2015, 12, 3, 7, 12, 13, 14, ZoneOffset.UTC)
      val lowerBoundForTestDataset = ZonedDateTime.of(2015, 11, 5, 7, 1, 2, 3, ZoneOffset.UTC)

      println(s"start date from $upperBoundForTestDataset")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = upperBoundForTestDataset,
          agentName = "main",
          tags = Tags()
        )
      )

      println(s"start date from $upperBoundForTestDataset + 29days")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = upperBoundForTestDataset.plus(29, ChronoUnit.DAYS),
          agentName = "main",
          tags = Tags()
        )
      )

      println(s"start date from $upperBoundForTestDataset + 31days")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = upperBoundForTestDataset.plus(31, ChronoUnit.DAYS),
          agentName = "main",
          tags = Tags()
        )
      )

      println(s"start date from $upperBoundForTestDataset - 1 day")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = upperBoundForTestDataset.minus(1, ChronoUnit.DAYS),
          agentName = "main",
          tags = Tags()
        )
      )

      println(s"start date from $lowerBoundForTestDataset")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = lowerBoundForTestDataset,
          agentName = "main",
          tags = Tags()
        )
      )

      println(s"start date from $lowerBoundForTestDataset + 1 day")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = lowerBoundForTestDataset.plus(1, ChronoUnit.DAYS),
          agentName = "main",
          tags = Tags()
        )
      )

      println(s"start date from $lowerBoundForTestDataset + 5 day")
      println(
        Co2LevelsAnalysis.findStartDate(
          influxDbClient,
          until = lowerBoundForTestDataset.plus(5, ChronoUnit.DAYS),
          agentName = "main",
          tags = Tags()
        )
      )

      ReturnStatus.success
    }
  }
}
