package ru.maizy.ambient7.analysis.test.notifications.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.analysis.notifications.watcher.DelayHelper
import ru.maizy.ambient7.analysis.notifications.watcher.DelayHelper.{ On, Off }
import ru.maizy.ambient7.analysis.test.BaseSpec

class Increment(val value: Int) extends AnyVal {
  override def toString: String = s"[$value]"
}

object Increment {
  def apply(value: Int): Increment = new Increment(value)
}

class DelayHelperSpec extends BaseSpec {

  var shift = 0

  def resetShift(): Unit = {
    shift = 0
    ()
  }

  def point(value: Int): (Increment, Int) = {
    val res = (Increment(shift), value)
    shift += 1
    res
  }

  def resPoint(shift: Int, value: Int, f: ((Increment, Int)) => Int): (Int, (Increment, Int), Int) = {
    val point = (Increment(shift), value)
    (shift, point, f(point))
  }

  "DelayHelper" should "detect simple growing value" in {

    def f(value: (Increment, Int)): Int = value._2 * 2

    val helper = new DelayHelper[(Increment, Int), Int](
      f = f,
      threshold = i => i >= 10,
      delay = 2,
      resolveDelay = 3
    )

    def res(shift: Int, value: Int) = resPoint(shift, value, f)

    shift = 0
    helper.processData(List(
      point(1), // 0
      point(1),
      point(1),
      point(5), // 3
      point(6), // 4, on
      point(7),
      point(8),
      point(0), // 7
      point(1),
      point(2), // 9, off
      point(3),
      point(4)
    )).toList shouldBe List(
      On(res(3, 5), res(5, 7)),
      Off(res(7, 0), res(9, 2), duration=Some(4))
    )
  }

  it should "be lazy and not growing call stack" in {
    val i100x100000 = Stream.continually(100).take(100000)
    val i200x5 = Stream.continually(200).take(5)
    val iterator = (i100x100000 ++ i200x5 ++ i100x100000).toIterable

    val helper = new DelayHelper[Int, Int](
      f = identity,
      threshold = i => i >= 200,
      delay = 3,
      resolveDelay = 1
    )

    helper.processData(iterator).toList shouldBe List(
      On((100000, 100000, 100000), (100002, 100002, 100002)),
      Off((100000, 100000, 100000), (100002, 100002, 100002), duration=Some(4))
    )
  }

}
