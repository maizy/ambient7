package ru.maizy.ambient7.rdbms

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{ ZoneOffset, ZonedDateTime }
import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._
import ru.maizy.ambient7.core.data.{ Co2Agent, Co2AggregatedLevels }
import ru.maizy.ambient7.core.util.DateTimeIterator
import ru.maizy.ambient7.core.util.Dates.dateTimeForUser

object Co2Service extends LazyLogging {

  val DB_ZONE = ZoneOffset.UTC
  val DB_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

  def detectStartDateTime(agentId: Co2Agent)(implicit db: DBSession): Either[String, Option[ZonedDateTime]] = {
    // TODO: readonly transaction

    val query = sql"""
      select day, hour
      from "co2_hourly_report"
      where agent_name = ${agentId.agentName}
        and tags_ordered = ${agentId.tags.encoded}
      order by day desc, hour desc
      limit 1
      """
    val res = Try(
      query
        .map(rs => (rs.date("day"), rs.int("hour")))
        .single.apply()
    )

    res match {
      case Success(None) => Right(None)
      case Success(Some((day, hour))) =>
        require(hour >= 0 && hour < 24)
        Right(Some(
          // last period from DB had analized yet, so add an hour
          dateTimeFromDb(day, hour).plus(1, ChronoUnit.HOURS)
        ))
      case Failure(e) =>
        Left(s"Unable to perform query: $e")
    }
  }

  def addOrUpdateAggregate(
      aggregate: Co2AggregatedLevels,
      agentId: Co2Agent)(implicit db: DBSession): Either[String, Unit] = {

    val day = dateTimeToDbDate(aggregate.from)
    val hour = dateTimeToDbHour(aggregate.from)

    logger.debug(s"upsert aggregate for ${dateTimeForUser(aggregate.from)}")
    val query = sql"""
      merge into "co2_hourly_report"
      (
        day, hour, tags_ordered, agent_name,
        low_level_total, medium_level_total, high_level_total, unknown_level_total
      )
      key(day, hour, agent_name, tags_ordered)
      values (
        $day, $hour, ${agentId.tags.encoded}, ${agentId.agentName},
        ${aggregate.lowLevel}, ${aggregate.mediumLevel}, ${aggregate.highLevel}, ${aggregate.unknownLevel}
      )"""

    Try(query.execute().apply()) match {
      case Success(_) => Right(())
      case Failure(e) => Left(s"Unable to upsert aggregation data: $e")
    }
  }

  def getHourlyAggregates(
      agentId: Co2Agent,
      from: ZonedDateTime,
      to: ZonedDateTime)(implicit db: DBSession): Seq[Co2AggregatedLevels] = {

    require(to.compareTo(from) >= 0)
    val timeZone = from.getZone

    val fromDay = dateTimeToDbDate(from)
    val fromHour = dateTimeToDbHour(from)
    val toDay = dateTimeToDbDate(to)
    val toHour = dateTimeToDbHour(to)

    val query = sql"""
      select day, hour, low_level_total, medium_level_total, high_level_total, unknown_level_total
      from "co2_hourly_report"
      where agent_name = ${agentId.agentName}
        and tags_ordered = ${agentId.tags.encoded}
        and (day > ${fromDay} or (day = ${fromDay} and hour >= ${fromHour}))
        and (day < ${toDay} or (day = ${toDay} and hour < ${toHour}))
      order by day, hour
      """

    def timeKey(time: ZonedDateTime): Long = {
      time.toEpochSecond
    }

    val dbResults =
      query
      .map { row =>
        val time = dateTimeFromDb(row.date("day"), row.int("hour")).withZoneSameInstant(timeZone)
        val levels = Co2AggregatedLevels(
          row.int("low_level_total"),
          row.int("medium_level_total"),
          row.int("high_level_total"),
          row.int("unknown_level_total"),
          time,
          time.plus(1, ChronoUnit.HOURS),
          agentId
        )
        timeKey(time) -> levels
      }
      .list.apply().toMap

    for (i <- DateTimeIterator(from, to, 1, ChronoUnit.HOURS).toSeq)
      yield dbResults.getOrElse(
        timeKey(i),
        Co2AggregatedLevels(0, 0, 0, 60, i, i.plus(1, ChronoUnit.HOURS), agentId)
      )
  }

  def computeDailyAggregates(
      agentId: Co2Agent,
      from: ZonedDateTime,
      to: ZonedDateTime)(implicit db: DBSession): Seq[Co2AggregatedLevels] = {

    @tailrec
    def iter(
        aggregates: Seq[Co2AggregatedLevels],
        nextDay: ZonedDateTime,
        resAggregate: Co2AggregatedLevels,
        res: Seq[Co2AggregatedLevels]): Seq[Co2AggregatedLevels] = {
      aggregates match {
        case Seq(elem, other @ _*) if elem.from.compareTo(nextDay) < 0 =>
          iter(other, nextDay, resAggregate.combine(elem), res)

        // the next day
        case Seq(elem, other @ _*) =>
          iter(other, elem.from.plusDays(1), elem, res :+ resAggregate)

        case Seq() =>
          res :+ resAggregate
      }
    }

    getHourlyAggregates(agentId, from, to) match {
      case Seq(first, other @ _*) =>
        iter(other, first.from.plusDays(1), first, List())
      case Seq() =>
        Seq.empty
    }
  }

  private def dateTimeFromDb(utcDbDay: java.sql.Date, hour: Int): ZonedDateTime =
    utcDbDay.toLocalDate.atTime(hour, 0).atZone(DB_ZONE)

  /**
   * note that time part is stripped
   */
  private def dateTimeToDbDate(dateTime: ZonedDateTime): String =
    dateTime.withZoneSameInstant(DB_ZONE).format(DB_DATE_FORMAT)

  /**
   * note that date part is stripped
   */
  private def dateTimeToDbHour(dateTime: ZonedDateTime): Int =
    dateTime.withZoneSameInstant(DB_ZONE).getHour

}
