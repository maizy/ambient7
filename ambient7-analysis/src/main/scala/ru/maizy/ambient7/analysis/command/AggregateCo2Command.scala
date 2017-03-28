package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._
import ru.maizy.ambient7.analysis.influxdb
import ru.maizy.ambient7.analysis.service.InfluxDbCo2Service
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.core.data.{ Co2Agent, Co2Device }
import ru.maizy.ambient7.core.util.Dates.dateTimeForUser
import ru.maizy.ambient7.rdbms.Co2Service
import ru.maizy.influxdbclient.InfluxDbClient

object AggregateCo2Command extends LazyLogging {

  def run(opts: Ambient7Options): ReturnStatus = {
    (influxdb.buildClient(opts), initDbSession(opts)) match {
      case (Some(influxDbClient), Some(dBSession)) =>
        val results: List[Boolean] = opts.devices
          .map(_.co2Devices).getOrElse(List.empty)
          .map(processDevice(_, influxDbClient, dBSession))
        if (results.exists { r => !r }) {
          logger.error("Some results haven't writen to DB")
          ReturnStatus.computeError
        } else {
          ReturnStatus.success
        }
      case _ => ReturnStatus.paramsError
    }
  }

  private def processDevice(device: Co2Device, influxDbClient: InfluxDbClient, dbSession: DBSession): Boolean = {
    implicit val implicitDbSession = dbSession
    val now = ZonedDateTime.now()
    // val now = ZonedDateTime.of(2015, 12, 3, 7, 12, 13, 14, ZoneOffset.UTC)
    val agent = device.agent
    logger.info(s"Process co2 device $device")

    val eitherStartDate: Either[String, ZonedDateTime] = detectStartDate(agent, influxDbClient, now)

    eitherStartDate match {
      case Left(error) =>
        logger.error(s"Unable to detect start date for analisys: $error")
        false

      case Right(startDate) =>
        val hourBefore = now.minusHours(1).truncatedTo(ChronoUnit.HOURS)
        logger.info(s"Analyse data until ${ dateTimeForUser(hourBefore) }")
        var anyFailed = false
        if (startDate.compareTo(hourBefore) < 0) {
          val analyseResults = InfluxDbCo2Service.analyseLevelsByHour(
            startDate, hourBefore, influxDbClient, agent
          )
          logger.info(s"Computed ${ analyseResults.size } new hourly results")
          if (analyseResults.nonEmpty) {
            logger.info("Write results to DB")
          }

          for (dayRes <- analyseResults.valuesIterator) {
            Co2Service.addOrUpdateAggregate(dayRes, agent).left.foreach { e =>
              anyFailed = true
              logger.error(s"Unable to write aggregate for ${ dayRes.from }, skipping: $e")
            }
          }
        }
        if (anyFailed) {
          logger.error("Some results haven't writen to DB")
          false
        } else {
          true
        }
    }
  }

  private def detectStartDate(
      agent: Co2Agent, influxDbClient: InfluxDbClient, now: ZonedDateTime)(implicit dBSession: DBSession) = {

    val eitherDbStartDate = Co2Service.detectStartDateTime(agent)
    val eitherStartDate: Either[String, ZonedDateTime] = eitherDbStartDate
      .right.flatMap {
      case None =>
        val influxDbStartDateTime = InfluxDbCo2Service.detectStartDateTime(
          influxDbClient,
          now,
          agent
        )
        influxDbStartDateTime.right.foreach { dateTime =>
          logger.info(s"Start date according to influxdb data is ${ dateTimeForUser(dateTime) }")
        }
        influxDbStartDateTime
      case Some(date) =>
        logger.info(s"Start date according to DB data is ${ dateTimeForUser(date) }")
        Right(date)
    }
    eitherStartDate
  }

  private def initDbSession(opts: Ambient7Options): Option[DBSession] = {
    opts.mainDb.flatMap { dbSetting =>
      dbSetting.url.map { url =>
        Class.forName("org.h2.Driver")
        ConnectionPool.singleton(url, dbSetting.user, dbSetting.password)
        AutoSession
      }
    }
  }

}
