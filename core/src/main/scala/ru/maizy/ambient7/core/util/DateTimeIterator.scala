package ru.maizy.ambient7.core.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// TODO: generalize for any date order past->future, future->past
class DateTimeIterator private (from: ZonedDateTime, to: ZonedDateTime, step: Long, stepUnit: ChronoUnit)
  extends Iterator[ZonedDateTime]
{
  var current = from
  override def hasNext: Boolean = current.compareTo(to) < 0

  override def next(): ZonedDateTime = {
    val res = current
    current = current.plus(step, stepUnit)
    res
  }
}

object DateTimeIterator {
  def apply(from: ZonedDateTime, to: ZonedDateTime, step: Long, stepUnit: ChronoUnit): DateTimeIterator = {
    new DateTimeIterator(from, to, step, stepUnit)
  }
}
