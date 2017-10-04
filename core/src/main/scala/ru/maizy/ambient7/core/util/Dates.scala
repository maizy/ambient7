package ru.maizy.ambient7.core.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.{ Instant, ZoneId, ZonedDateTime }
import scala.concurrent.duration.Duration

object Dates {
  def dateTimeForUser(dateTime: ZonedDateTime, timeZone: ZoneId = ZoneId.systemDefault()): String =
    dateTime.withZoneSameInstant(timeZone).toString

  def truncateDateTime(dateTime: ZonedDateTime, step: Long): ZonedDateTime = {
    val epochSeconds = dateTime.toEpochSecond
    val mod = epochSeconds % step
    val instant = Instant.ofEpochSecond(epochSeconds - mod)
    ZonedDateTime.ofInstant(instant, dateTime.getZone)
  }

  def truncateDateTime(dateTime: ZonedDateTime, step: Duration): ZonedDateTime = {
    require(step.isFinite)
    truncateDateTime(dateTime, step.toSeconds)
  }
}
