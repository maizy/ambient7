package ru.maizy.influxdbclient.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.{ Instant, ZoneId, ZoneOffset, ZonedDateTime }
import scala.util.Try

object Dates {

  val INFLUXDB_TIMEZONE = ZoneOffset.UTC
  val INFLUXDB_DATETIME_FORMAT = DateTimeFormatter.ISO_INSTANT
  private val SYSTEM_TIMEZONE = ZoneId.systemDefault()

  def toInfluxDbFormat(date: ZonedDateTime): String =
    date.withZoneSameInstant(INFLUXDB_TIMEZONE).format(INFLUXDB_DATETIME_FORMAT)

  def fromInfluxDbToZonedDateTime(dateTime: String, timeZone: ZoneId = Dates.SYSTEM_TIMEZONE): Try[ZonedDateTime] =
    Try(INFLUXDB_DATETIME_FORMAT.parse(dateTime))
      .map(Instant.from(_).atZone(INFLUXDB_TIMEZONE).withZoneSameInstant(timeZone))
}
