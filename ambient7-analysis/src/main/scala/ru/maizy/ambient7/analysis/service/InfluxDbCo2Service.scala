package ru.maizy.ambient7.analysis.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.util.{ Failure, Success }
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.ambient7.core.data.{ Co2AggregatedLevels, MT8057AgentId }
import ru.maizy.influxdbclient.data.{ NullValue, SeriesItem, StringValue }
import ru.maizy.influxdbclient.util.Dates
import ru.maizy.influxdbclient.util.Escape.{ escapeValue, tagsToQueryCondition }
import ru.maizy.influxdbclient.InfluxDbClient

object InfluxDbCo2Service extends LazyLogging {

  val CO2_OK = 800
  val CO2_NOT_OK = 1200

  val DEFAULT_MAX_EMPTY_DURATION = 30

  def computeLevels(
      influxDbClient: InfluxDbClient,
      from: ZonedDateTime,
      until: ZonedDateTime,
      agentId: MT8057AgentId): Either[String, Co2AggregatedLevels] = {

    require(from.compareTo(until) < 0)

    val dateFrom = Dates.toInfluxDbFormat(from)
    val dateTo = Dates.toInfluxDbFormat(until)

    val query =
      "select max(ppm) as max_ppm " +
      "from co2 " +
      s"where time >= ${escapeValue(dateFrom)} and time < ${escapeValue(dateTo)} " +
      s"and agent = ${escapeValue(agentId.agentName)} and ${tagsToQueryCondition(agentId.tags.asPairs)} " +
      "group by time(1m)"

    influxDbClient
      .query(query)
      .left.map(e => s"error when requesting data from influxdb: $e")
      .right.flatMap { results =>
        val totalMinutes = time.Duration.between(from, until).toMinutes.toInt
        results.headOption.flatMap(_.headOption) match {

          case Some(series) =>
            series.getColumnNumberValues("max_ppm")
              .right.map { perMinuteLevels =>
                Co2AggregatedLevels(
                  lowLevel = perMinuteLevels.count(_.toInt < CO2_OK),
                  mediumLevel =  perMinuteLevels.count(l => l.toInt >= CO2_OK && l.toInt < CO2_NOT_OK),
                  highLevel = perMinuteLevels.count(_.toInt >= CO2_NOT_OK),
                  unknownLevel = totalMinutes - perMinuteLevels.size,
                  from = from,
                  to = until,
                  agentId = agentId
                )
              }

          case _ => Right(
            Co2AggregatedLevels(
              lowLevel = 0,
              mediumLevel =  0,
              highLevel = 0,
              unknownLevel = totalMinutes,
              from = from,
              to = until,
              agentId = agentId
            )
          )
        }
      }
  }

  /**
   * note that an until date truncated to hours
   */
  def detectStartDateTime(
      influxDbClient: InfluxDbClient,
      until: ZonedDateTime,
      agentId: MT8057AgentId,
      maxEmptyDuration: Int = DEFAULT_MAX_EMPTY_DURATION): Either[String, ZonedDateTime] = {

    val tagsConditions = tagsToQueryCondition(agentId.tags.asPairs)
    val agentNameEscaped = escapeValue(agentId.agentName)
    val untilTruncated = until.truncatedTo(ChronoUnit.DAYS)

    def buildQuery(lowerBound: ZonedDateTime, upperBound: ZonedDateTime): String = {
      "select time, max(ppm) as max_ppm " +
      "from co2 " +
      s"where time >= ${escapeValue(Dates.toInfluxDbFormat(lowerBound))} "+
      s"and time < ${escapeValue(Dates.toInfluxDbFormat(upperBound))} " +
      s"and agent = $agentNameEscaped and $tagsConditions " +
      "group by time(1h)"
    }

    @tailrec
    def find(
        upperBound: ZonedDateTime,
        res: ZonedDateTime = until,
        noDataCount: Int = 0,
        lastNonEmptyResults: Option[SeriesItem] = None): Either[String, ZonedDateTime] = {

      val lowerBound = upperBound.minusDays(1).truncatedTo(ChronoUnit.DAYS)
      val query = buildQuery(lowerBound, upperBound)
      influxDbClient.query(query) match {
        case Left(error) =>
          Left(s"Unable to perform query for $lowerBound - $upperBound: ${error.message}")

        case Right(result) =>
          result.firstSeriesItemIfNumberColumnNotEmpty("max_ppm") match {
            case Some(seriesItem) =>
              find(lowerBound, lowerBound, 0, Some(seriesItem))

            case None =>
              if (noDataCount >= maxEmptyDuration) {
                Right(findStartHour(res, lastNonEmptyResults))
              } else {
                find(lowerBound, res, noDataCount + 1, lastNonEmptyResults)
              }
          }

      }
    }

    def findStartHour(day: ZonedDateTime, mayBeDayResults: Option[SeriesItem]): ZonedDateTime = {
      mayBeDayResults match {
        case None => day
        case Some(dayResults) =>
          // results start from some hour, iterate from begining of day & find first non empty hour
          val firstHour = dayResults.values
            .find { row =>
              row.size == 2 && row(1) != NullValue
            }
            .map(row => row(0))
          firstHour match {
            case Some(rawDateTime: StringValue) =>
              Dates.fromInfluxDbToZonedDateTime(rawDateTime.value) match {
                case Success(dateTime) =>
                  // sync timezones & replace hour
                  dateTime.withZoneSameInstant(day.getZone).truncatedTo(ChronoUnit.HOURS)

                case Failure(_) => day
              }
            case _ => day
          }
      }
    }

    find(untilTruncated)
  }

  def analyseLevelsByHour(
      startDate: ZonedDateTime,
      until: ZonedDateTime,
      influxDbClient: InfluxDbClient,
      agentId: MT8057AgentId): Map[ZonedDateTime, Co2AggregatedLevels] = {

      @tailrec
      def iter(
          from: ZonedDateTime,
          res: Map[ZonedDateTime, Co2AggregatedLevels]): Map[ZonedDateTime, Co2AggregatedLevels] = {

        val to = from.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        computeLevels(influxDbClient, from, to, agentId) match {
          case _ if to.compareTo(until) >= 0 =>
            res

          case Left(error) =>
            logger.warn(s"Unable to analyse ${from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}, skipping: $error")
            iter(to, res)

          case Right(aggregate) if !aggregate.hasAnyResult =>
            logger.warn(s"There is no results for ${from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}, skipping")
            iter(to, res)

          case Right(aggregate) =>
            iter(to, res + (from -> aggregate))
        }
      }

      iter(startDate, ListMap.empty)
    }
}
