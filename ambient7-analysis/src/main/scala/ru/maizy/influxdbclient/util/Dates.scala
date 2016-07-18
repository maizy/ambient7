package ru.maizy.influxdbclient.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.{ ZoneOffset, ZonedDateTime }

object Dates {
  def toInfluxDbFormat(date: ZonedDateTime): String =
    date.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
}
