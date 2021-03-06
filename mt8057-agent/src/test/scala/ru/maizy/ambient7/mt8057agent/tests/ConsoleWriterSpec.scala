package ru.maizy.ambient7.mt8057agent.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.mt8057agent.{ Co2, ConsoleWriter, Temp }
import ru.maizy.ambient7.mt8057agent.{ Co2Updated, DeviceDown, DeviceUp, TempUpdated }

class ConsoleWriterSpec extends AbstractBaseSpec with WritersTestUtils {

  "ConsoleWriter" should "do nothing on init" in {
    val w = new ConsoleWriter(Ambient7Options())
    checkWriterInit(w) should be (("", ""))
  }

  it should "output device up & down events to stderr" in {
    val w1 = new ConsoleWriter(Ambient7Options())
    checkWriterEvent(w1, DeviceUp(time)) should be (("", s"$formatedTime: device connected\n"))

    val w2 = new ConsoleWriter(Ambient7Options())
    checkWriterEvent(w2, DeviceUp(time - 1L))
    checkWriterEvent(w2, DeviceDown(time)) should be (("", s"$formatedTime: device disconnected\n"))
  }

  it should "output co2 updates" in {
    val w = new ConsoleWriter(Ambient7Options())
    val co2 = Co2(300)
    checkWriterEvent(w, DeviceUp(time - 2L))
    checkWriterEvent(w, Co2Updated(co2, time - 1L)) should be ((s"$formatedTime: co2=300\n", s""))
  }

  it should "output temp updates" in {
    val w = new ConsoleWriter(Ambient7Options())
    val temp = Temp(23.11943432)
    checkWriterEvent(w, DeviceUp(time - 2L))
    checkWriterEvent(w, TempUpdated(temp, time)) should be ((s"$formatedTime: temp=23.12\n", s""))
  }

}
