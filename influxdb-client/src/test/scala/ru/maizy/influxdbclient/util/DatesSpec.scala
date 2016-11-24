package ru.maizy.influxdbclient.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ ZoneId, ZoneOffset, ZonedDateTime }
import scala.util.{ Failure, Success }
import ru.maizy.influxdbclient.BaseSpec

class DatesSpec extends BaseSpec {

  "Dates.fromInfluxDbToZonedDateTime" should "parse influxdb dates values" in {
    val expectedUtc = ZonedDateTime.of(2015, 11, 5, 17, 59, 12, 0, ZoneOffset.UTC)
    val inLocalTime = expectedUtc.withZoneSameInstant(ZoneId.systemDefault())
    Dates.fromInfluxDbToZonedDateTime("2015-11-05T17:59:12Z") shouldBe Success(inLocalTime)
  }

  it should "return time in propert time zone" in {
    val expectedUtc = ZonedDateTime.of(2015, 11, 5, 17, 59, 12, 0, ZoneOffset.UTC)
    val cetZone = ZoneId.of("CET")
    val inCetZone = expectedUtc.withZoneSameInstant(cetZone)
    val parsedDateTime = Dates.fromInfluxDbToZonedDateTime("2015-11-05T17:59:12Z", cetZone)
    parsedDateTime shouldBe Success(inCetZone)
    parsedDateTime.get.getZone shouldBe cetZone
  }

  it should "parse leap-year dates" in {
    val expectedUtc = ZonedDateTime.of(2016, 2, 29, 12, 1, 2, 0, ZoneOffset.UTC)
    val inLocalTime = expectedUtc.withZoneSameInstant(ZoneId.systemDefault())
    Dates.fromInfluxDbToZonedDateTime("2016-02-29T12:01:02Z") shouldBe Success(inLocalTime)
  }

  it should "return failure for wrong dates" in {
    Dates.fromInfluxDbToZonedDateTime("2016-02-29") shouldBe a[Failure[_]]
  }

  "Dates.toInfluxDbFormat" should "return datetime in influxdb format & zone" in {
    val utc = ZonedDateTime.of(2015, 11, 5, 17, 59, 12, 0, ZoneOffset.UTC)
    val local = utc.withZoneSameInstant(ZoneId.systemDefault())
    Dates.toInfluxDbFormat(utc) shouldBe "2015-11-05T17:59:12Z"
    Dates.toInfluxDbFormat(local) shouldBe "2015-11-05T17:59:12Z"
  }

}
