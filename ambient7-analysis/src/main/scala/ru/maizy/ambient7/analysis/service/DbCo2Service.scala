package ru.maizy.ambient7.analysis.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{ ZoneOffset, ZonedDateTime }
import scala.util.{ Failure, Success, Try }
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.ambient7.core.data.MT8057AgentId
import scalikejdbc._
import ru.maizy.ambient7.core.util.Dates.dateTimeForUser

object DbCo2Service extends LazyLogging {

  val DB_ZONE = ZoneOffset.UTC
  val DB_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE

  def detectStartDateTime(agentId: MT8057AgentId)(implicit db: DBSession): Either[String, Option[ZonedDateTime]] = {
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

  def addOrUpdateAggregate(
      aggregate: Co2AgregatedLevels,
      agentId: MT8057AgentId)(implicit db: DBSession): Either[String, Unit] = {

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
      case Success(_) => Right(Unit)
      case Failure(e) => Left(s"Unable to upsert aggregation data: $e")
    }
  }

}
