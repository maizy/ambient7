package ru.maizy.ambient7.core.tests.util

import java.time.temporal.ChronoUnit
import java.time.{ ZoneOffset, ZonedDateTime }
import ru.maizy.ambient7.core.tests.BaseSpec
import ru.maizy.ambient7.core.util.DateTimeIterator

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
class DateTimeIteratorSpec extends BaseSpec {

  val base = ZonedDateTime.of(2014, 5, 20, 19, 15, 13, 0, ZoneOffset.UTC)
  val plusOneHour = ZonedDateTime.of(2014, 5, 20, 20, 15, 13, 0, ZoneOffset.UTC)
  val plusOneHourAndMore = ZonedDateTime.of(2014, 5, 20, 20, 17, 15, 20, ZoneOffset.UTC)
  val plus6HoursAndMore = ZonedDateTime.of(2014, 5, 21, 1, 19, 59, 59, ZoneOffset.UTC)

  "DateTimeIterator" should "return empty iterator if dates equals" in {
    DateTimeIterator(base, base, 1, ChronoUnit.HOURS).toList shouldBe List()
  }

  it should "return iterator with one item (one step)" in {
    DateTimeIterator(base, plusOneHour, 1, ChronoUnit.HOURS).toList shouldBe List(base)
  }

  it should "include upper bound if it not exactly match some step" in {
    DateTimeIterator(base, plusOneHourAndMore, 1, ChronoUnit.HOURS).toList shouldBe List(base, plusOneHour)
  }

  it should "return iterator with some steps" in {
    DateTimeIterator(base, plus6HoursAndMore, 1, ChronoUnit.HOURS).toList shouldBe List(
      ZonedDateTime.of(2014, 5, 20, 19, 15, 13, 0, ZoneOffset.UTC),
      ZonedDateTime.of(2014, 5, 20, 20, 15, 13, 0, ZoneOffset.UTC),
      ZonedDateTime.of(2014, 5, 20, 21, 15, 13, 0, ZoneOffset.UTC),
      ZonedDateTime.of(2014, 5, 20, 22, 15, 13, 0, ZoneOffset.UTC),
      ZonedDateTime.of(2014, 5, 20, 23, 15, 13, 0, ZoneOffset.UTC),
      ZonedDateTime.of(2014, 5, 21, 0, 15, 13, 0, ZoneOffset.UTC),
      ZonedDateTime.of(2014, 5, 21, 1, 15, 13, 0, ZoneOffset.UTC)
    )
  }
}
