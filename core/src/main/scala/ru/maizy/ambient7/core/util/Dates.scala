package ru.maizy.ambient7.core.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ ZoneId, ZonedDateTime }

object Dates {
  def dateTimeForUser(dateTime: ZonedDateTime): String =
    dateTime.withZoneSameInstant(ZoneId.systemDefault()).toString
}
