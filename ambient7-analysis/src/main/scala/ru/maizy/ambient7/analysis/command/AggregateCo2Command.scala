package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.DurationInt
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._
import ru.maizy.ambient7.analysis.AppOptions
import ru.maizy.ambient7.analysis.service.InfluxDbCo2Service
import ru.maizy.ambient7.core.data.Co2Agent
import ru.maizy.ambient7.core.util.Dates.dateTimeForUser
import ru.maizy.ambient7.rdbms.Co2Service
import ru.maizy.influxdbclient.{ InfluxDbClient, InfluxDbConnectionSettings }

object AggregateCo2Command extends LazyLogging {

  def run(opts: AppOptions): ReturnStatus = {
    if (opts.influxDbDatabase.isEmpty || opts.influxDbReadonlyBaseUrl.isEmpty) {
      ReturnStatus.paramsError
    } else {
      val influxDbClient: InfluxDbClient = initInfluxDbClient(opts)
      implicit val dbSession = initDbSession(opts)

      val now = ZonedDateTime.now()
      // val now = ZonedDateTime.of(2015, 12, 3, 7, 12, 13, 14, ZoneOffset.UTC)
      val agentId = Co2Agent(opts.influxDbAgentName, opts.influxDbTags)

      val eitherDbStartDate = Co2Service.detectStartDateTime(agentId)

      val eitherStartDate: Either[String, ZonedDateTime] = eitherDbStartDate
        .right.flatMap {
          case None =>
            val influxDbStartDateTime = InfluxDbCo2Service.detectStartDateTime(
              influxDbClient,
              now,
              agentId
            )
            influxDbStartDateTime match {
              case Right(dateTime) =>
                logger.info(s"Start date according to influxdb data is ${dateTimeForUser(dateTime)}")
              case _ =>
            }
            influxDbStartDateTime
          case Some(date) =>
            logger.info(s"Start date according to DB data is ${dateTimeForUser(date)}")
            Right(date)
        }

      eitherStartDate match {
        case Left(error) =>
          logger.error(s"Unable to detect start date for analisys: $error")
          ReturnStatus.computeError

        case Right(startDate) =>
          val hourBefore = now.minusHours(1).truncatedTo(ChronoUnit.HOURS)
          logger.info(s"Analyse data until ${dateTimeForUser(hourBefore)}")
          var anyFailed = false
          if (startDate.compareTo(hourBefore) < 0) {
            val analyseResults = InfluxDbCo2Service.analyseLevelsByHour(
              startDate, hourBefore, influxDbClient, agentId
            )
            logger.info(s"Computed ${analyseResults.size} new hourly results")
            if (analyseResults.nonEmpty) {
              logger.info("Write results to DB")

            }

            for (dayRes <- analyseResults.valuesIterator) {
              Co2Service.addOrUpdateAggregate(dayRes, agentId) match {
                case Left(e) =>
                  anyFailed = true
                  logger.error(s"Unable to write aggregate for ${dayRes.from}, skipping: $e")
                case _ =>
              }
            }
          }
          if (anyFailed) {
            logger.error("Some results haven't writen to DB")
            ReturnStatus.computeError
          } else {
            ReturnStatus.success
          }
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

  private def initDbSession(opts: AppOptions): DBSession = {
    Class.forName("org.h2.Driver")
    ConnectionPool.singleton(opts.dbUrl, opts.dbUser, opts.dbPassword)
    AutoSession
  }

}
