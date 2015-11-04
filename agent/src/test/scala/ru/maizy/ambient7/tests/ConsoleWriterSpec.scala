package ru.maizy.ambient7.tests

import ru.maizy.ambient7._

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class ConsoleWriterSpec extends AbstractBaseSpec with WritersTestUtils {

  "ConsoleWriter" should "do nothing on init" in {
    val w = new ConsoleWriter(AppOptions())
    checkWriterInit(w) should be (("", ""))
  }

  it should "output device up & down events to stderr" in {
    val w1 = new ConsoleWriter(AppOptions())
    checkWriterEvent(w1, DeviceUp(time)) should be (("", s"$formatedTime: device connected\n"))

    val w2 = new ConsoleWriter(AppOptions())
    checkWriterEvent(w2, DeviceUp(time - 1L))
    checkWriterEvent(w2, DeviceDown(time)) should be (("", s"$formatedTime: device disconnected\n"))
  }

  it should "output co2 updates" in {
    val w = new ConsoleWriter(AppOptions())
    val co2 = Co2(300)
    checkWriterEvent(w, DeviceUp(time - 2L))
    checkWriterEvent(w, Co2Updated(co2, time - 1L)) should be ((s"$formatedTime: co2=300\n", s""))
  }

  it should "output temp updates" in {
    val w = new ConsoleWriter(AppOptions())
    val temp = Temp(23.11943432)
    checkWriterEvent(w, DeviceUp(time - 2L))
    checkWriterEvent(w, TempUpdated(temp, time)) should be ((s"$formatedTime: temp=23.12\n", s""))
  }

}
