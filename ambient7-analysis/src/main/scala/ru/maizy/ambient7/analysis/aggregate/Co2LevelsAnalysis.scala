package ru.maizy.ambient7.analysis.aggregate

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import scala.annotation.tailrec
import ru.maizy.influxdbclient.data.SeriesItem
import ru.maizy.influxdbclient.util.Dates
import ru.maizy.influxdbclient.{ InfluxDbClient, Tags }
import ru.maizy.influxdbclient.util.Escape.escapeValue

case class Co2LevelsAggregate(
    lowLevel: Int,
    mediumLevel: Int,
    highLevel: Int,
    unknownLevel: Int,
    from: ZonedDateTime,
    to: ZonedDateTime,
    agentName: String,
    tags: Tags
) {
  override def toString: String = s"Co2LevelsAggregate(low=$lowLevel, med=$mediumLevel, high=$highLevel, " +
    s"unknown=$unknownLevel, $from->$to, agent=$agentName, tags=$tags)"

  def hasAnyResult: Boolean =
    lowLevel > 0 || mediumLevel > 0 || highLevel > 0
}

object Co2LevelsAnalysis {

  val CO2_OK = 800
  val CO2_NOT_OK = 1200

  val DEFAULT_MAX_EMPTY_DURATION = 30

  private def truncateDate(date: ZonedDateTime): ZonedDateTime =
    date.truncatedTo(ChronoUnit.DAYS)

  def compute(
    influxDbClient: InfluxDbClient,
    from: ZonedDateTime,
    until: ZonedDateTime,
    agentName: String,
    tags: Tags): Either[String, Co2LevelsAggregate] = {

    require(from.compareTo(until) < 0)

    val dateFrom = Dates.toInfluxDbFormat(from)
    val dateTo = Dates.toInfluxDbFormat(until)

    val query =
      "select max(ppm) as max_ppm " +
      "from co2 " +
      s"where time >= ${escapeValue(dateFrom)} and time < ${escapeValue(dateTo)} " +
      s"and agent = ${escapeValue(agentName)} and ${tags.asQueryCondition} " +
      "group by time(1m)"

    influxDbClient
      .query(query)
      .left.map(e => s"error when requesting data from influxdb: $e")
      .right.flatMap { results =>
        val totalMinutes = time.Duration.between(from, until).toMinutes.toInt
        results.headOption.flatMap(_.headOption) match {

          case Some(series) =>
            series.getNumberColumn("max_ppm")
              .right.map { perMinuteLevels =>
                Co2LevelsAggregate(
                  lowLevel = perMinuteLevels.count(_.toInt < CO2_OK),
                  mediumLevel =  perMinuteLevels.count(l => l.toInt >= CO2_OK && l.toInt < CO2_NOT_OK),
                  highLevel = perMinuteLevels.count(_.toInt >= CO2_NOT_OK),
                  unknownLevel = totalMinutes - perMinuteLevels.size,
                  from = from,
                  to = until,
                  agentName = agentName,
                  tags = tags
                )
              }

          case _ => Right(
            Co2LevelsAggregate(
              lowLevel = 0,
              mediumLevel =  0,
              highLevel = 0,
              unknownLevel = totalMinutes,
              from = from,
              to = until,
              agentName = agentName,
              tags = tags
            )
          )
        }
      }
  }

  /**
   * note that an until date truncated to days
   */
  def findStartDate(
    influxDbClient: InfluxDbClient,
    until: ZonedDateTime,
    agentName: String,
    tags: Tags,
    maxEmptyDuration: Int = DEFAULT_MAX_EMPTY_DURATION): Either[String, ZonedDateTime] = {

    val tagsConditions = tags.asQueryCondition
    val agentNameEscaped = escapeValue(agentName)
    val untilTruncated = truncateDate(until)

    @tailrec
    def iter(upperBound: ZonedDateTime, res: ZonedDateTime, noDataCount: Int = 0): Either[String, ZonedDateTime] = {
      val lowerBound = upperBound.minusDays(1)
      val query =
        "select max(ppm) as max_ppm " +
        "from co2 " +
        s"where time >= ${escapeValue(Dates.toInfluxDbFormat(lowerBound))} "+
        s"and time < ${escapeValue(Dates.toInfluxDbFormat(upperBound))} " +
        s"and agent = $agentNameEscaped and $tagsConditions " +
        "group by time(1h)"

      def isEmptyResult(res: SeriesItem): Boolean = {
        res.getNumberColumn("max_ppm")
          .right.toOption.exists(_.isEmpty)
      }

      influxDbClient.query(query) match {
        case Left(error) => Left(s"Unable to perform query for $lowerBound - $upperBound: ${error.message}")
        case Right(result) =>
          val mayBeItems = result.headOption.flatMap(_.headOption)
          if (mayBeItems.isEmpty || isEmptyResult(mayBeItems.get)) {
            if (noDataCount >= maxEmptyDuration) {
              Right(res)
            } else {
              iter(lowerBound, res, noDataCount + 1)
            }
          } else {
            iter(lowerBound, lowerBound, 0)
          }
      }
    }

    iter(untilTruncated, untilTruncated)
  }
}
