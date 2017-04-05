package ru.maizy.ambient7.core.tests.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.DurationInt
import java.time.{ Instant, ZoneId, ZoneOffset, ZonedDateTime }
import ru.maizy.ambient7.core.tests.BaseSpec
import ru.maizy.ambient7.core.util.Dates


class DatesSpec extends BaseSpec {

  // timestamp = 1483272917
  val time = ZonedDateTime.of(2017, 1, 1, 12, 15, 17, 115, ZoneOffset.UTC)

  "Dates.truncateDateTime" should "works" in {
    Dates.truncateDateTime(time, 10.seconds) shouldBe
      ZonedDateTime.of(2017, 1, 1, 12, 15, 10, 0, ZoneOffset.UTC)
  }

  it should "works with long offsets" in {
    Dates.truncateDateTime(time, 1000.seconds) shouldBe
      ZonedDateTime.ofInstant(Instant.ofEpochSecond(1483272000L), ZoneOffset.UTC)
  }

  it should "saves zone information" in {
    val zone = ZoneId.systemDefault
    Dates.truncateDateTime(time.withZoneSameInstant(zone), 10.seconds).getZone shouldBe zone
  }
}
