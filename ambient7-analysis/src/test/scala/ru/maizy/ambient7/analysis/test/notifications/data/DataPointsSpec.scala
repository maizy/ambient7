package ru.maizy.ambient7.analysis.test.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import ru.maizy.ambient7.analysis.notifications.data.DataPoints
import ru.maizy.ambient7.analysis.test.BaseSpec


class DataPointsSpec extends BaseSpec {

  val now = ZonedDateTime.now()

  def shift(minutes: Long): ZonedDateTime = now.plusMinutes(minutes)

  "LimitedBufferWithTimestamp" should "support right ordered elements appending and traversing" in {
    val buffer = new DataPoints[String](limit = 10)
    buffer.appendPoint(shift(0), "A")
    buffer.appendPoint(shift(1), "B")
    buffer.appendPoint(shift(2), "C")

    buffer.toTraversable.toList shouldBe List(
      (shift(0), "A"),
      (shift(1), "B"),
      (shift(2), "C")
    )
  }

  it should "support unordered elements appending and traversing" in {
    val buffer = new DataPoints[String](limit = 10)
    buffer.appendPoint(shift(0), "A")
    buffer.appendPoint(shift(2), "C")
    buffer.appendPoint(shift(1), "B")

    buffer.toTraversable.toList shouldBe List(
      (shift(0), "A"),
      (shift(1), "B"),
      (shift(2), "C")
    )
  }

  it should "follow limit" in {
    val buffer = new DataPoints[String](limit = 2)
    buffer.appendPoint(shift(0), "A")
    buffer.appendPoint(shift(1), "B")
    buffer.appendPoint(shift(2), "C")

    buffer.toTraversable.toList shouldBe List(
      (shift(1), "B"),
      (shift(2), "C")
    )
  }

  it should "follow limit when unordered elements" in {
    val buffer = new DataPoints[String](limit = 2)
    buffer.appendPoint(shift(0), "A")
    buffer.appendPoint(shift(2), "B")
    buffer.appendPoint(shift(1), "C")

    buffer.toTraversable.toList shouldBe List(
      (shift(1), "C"),
      (shift(2), "B")
    )
  }

  it should "replace element with the same time" in {
    val buffer = new DataPoints[String](limit = 2)
    buffer.appendPoint(shift(0), "A")
    buffer.appendPoint(shift(0), "B")
    buffer.appendPoint(shift(1), "C")

    buffer.toTraversable.toList shouldBe List(
      (shift(0), "B"),
      (shift(1), "C")
    )
  }

  it should "replace element with the same time if elements inserted unordered" in {
    val buffer1 = new DataPoints[String](limit = 2)
    buffer1.appendPoint(shift(1), "C")
    buffer1.appendPoint(shift(0), "A")
    buffer1.appendPoint(shift(0), "B")

    buffer1.toTraversable.toList shouldBe List(
      (shift(0), "B"),
      (shift(1), "C")
    )

    val buffer2 = new DataPoints[String](limit = 2)
    buffer2.appendPoint(shift(0), "A")
    buffer2.appendPoint(shift(1), "C")
    buffer2.appendPoint(shift(0), "B")

    buffer2.toTraversable.toList shouldBe List(
      (shift(0), "B"),
      (shift(1), "C")
    )
  }
}
