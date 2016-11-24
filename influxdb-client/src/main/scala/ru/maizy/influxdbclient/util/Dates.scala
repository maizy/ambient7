package ru.maizy.influxdbclient.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.{ Instant, ZoneId, ZoneOffset, ZonedDateTime }
import scala.util.Try

object Dates {

  val INFLUXDB_TIMEZONE = ZoneOffset.UTC
  val INFLUXDB_DATETIME_FORMAT = DateTimeFormatter.ISO_INSTANT

  def toInfluxDbFormat(date: ZonedDateTime): String =
    date.withZoneSameInstant(INFLUXDB_TIMEZONE).format(INFLUXDB_DATETIME_FORMAT)

  def fromInfluxDbToZonedDateTime(dateTime: String, timeZone: ZoneId = ZoneId.systemDefault()): Try[ZonedDateTime] =
    Try(INFLUXDB_DATETIME_FORMAT.parse(dateTime))
      .map(Instant.from(_).atZone(INFLUXDB_TIMEZONE).withZoneSameInstant(timeZone))
}
