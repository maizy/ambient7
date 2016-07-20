package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{ ZoneId, ZoneOffset, ZonedDateTime }
import scala.annotation.tailrec
import scala.concurrent.duration.DurationInt
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.influxdbclient.{ InfluxDbClient, InfluxDbConnectionSettings, Tags }
import ru.maizy.ambient7.analysis.AppOptions
import ru.maizy.ambient7.analysis.aggregate.{ Co2LevelsAggregate, Co2LevelsAnalysis }

object AggregateCo2Command extends LazyLogging {

  def run(opts: AppOptions): ReturnStatus = {
    if (opts.influxDbDatabase.isEmpty || opts.influxDbReadonlyBaseUrl.isEmpty) {
      ReturnStatus.paramsError
    } else {
      val influxDbClient: InfluxDbClient = initInfluxDbClient(opts)

      val upperBoundForTestDataset = ZonedDateTime.of(2015, 12, 3, 7, 12, 13, 14, ZoneOffset.UTC)
      val lowerBoundForTestDataset = ZonedDateTime.of(2015, 11, 5, 7, 1, 2, 3, ZoneOffset.UTC)

      val now = upperBoundForTestDataset.withZoneSameInstant(ZoneId.of("Europe/Moscow"))

      // TODO: from db
      val eitherDbStartDate: Either[String, Option[ZonedDateTime]] = Right(None)

      val eitherStartDate: Either[String, ZonedDateTime] = eitherDbStartDate
        .right.flatMap {
          case None => Co2LevelsAnalysis.findStartDate(
            influxDbClient,
            now,
            agentName = opts.influxDbAgentName,
            tags = opts.influxDbTags
          )
          case Some(date) =>
            logger.info(s"Start date according to DB data is ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
            Right(date)
        }

      eitherStartDate match {
        case Left(error) =>
          logger.error(s"Unable to detect start date for analisys $error")
          ReturnStatus.computeError

        case Right(startDate) =>
          logger.info(s"Start date detected from influxdb is ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
          val yesterday = now.minusDays(1).truncatedTo(ChronoUnit.DAYS)
          if (startDate.compareTo(yesterday) < 0) {
            val analyseResults = analyseByDate(
              startDate, yesterday, influxDbClient, opts.influxDbAgentName, opts.influxDbTags
            )
            for ((day, dayRes) <- analyseResults) {
              def pad(v: Int) = f"${v.toString}%5s"
              println(
                s"${day.format(DateTimeFormatter.ISO_LOCAL_DATE)}\t" +
                s"l: ${pad(dayRes.lowLevel)}\tm: ${pad(dayRes.mediumLevel)}\th: ${pad(dayRes.highLevel)}\t" +
                s"?: ${pad(dayRes.unknownLevel)}"
              )
            }
          }
          ReturnStatus.success
      }

    }

  }

  private def initInfluxDbClient(opts: AppOptions): InfluxDbClient = {
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

    new InfluxDbClient(
      influxDbSettings = writableSettings,
      _influxDbReadonlySettings = Some(readonlySettings),
      userAgent = Some("ambient7-analysis"),
      connectTimeout = 500.millis,
      readTimeout = 10.seconds
    )
  }

  private def analyseByDate(
    startDate: ZonedDateTime,
    until: ZonedDateTime,
    influxDbClient: InfluxDbClient,
    agentName: String, tags: Tags): Map[ZonedDateTime, Co2LevelsAggregate] = {

      @tailrec
      def iter(
        from: ZonedDateTime,
        res: Map[ZonedDateTime, Co2LevelsAggregate]): Map[ZonedDateTime, Co2LevelsAggregate] = {

        val to = from.plusDays(1).truncatedTo(ChronoUnit.DAYS)
        Co2LevelsAnalysis.compute(influxDbClient, from, to, agentName, tags) match {
          case Left(error) =>
            logger.warn(s"Unable to analyse ${from.format(DateTimeFormatter.ISO_LOCAL_DATE)}, skipping")
            iter(until, res)

          case Right(aggregate) if !aggregate.hasAnyResult =>
            logger.warn(s"There is not results for ${from.format(DateTimeFormatter.ISO_LOCAL_DATE)}, skipping")
            iter(until, res)

          case Right(aggregate) if to.compareTo(until) >= 0 =>
            res

          case Right(aggregate) =>
            iter(to, res + (from -> aggregate))
        }
      }

      iter(startDate, Map.empty)
    }
}
