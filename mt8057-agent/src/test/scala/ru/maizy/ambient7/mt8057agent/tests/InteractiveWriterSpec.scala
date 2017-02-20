package ru.maizy.ambient7.mt8057agent.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.mt8057agent.{ Co2, Co2Updated, DeviceUp, InteractiveWriter, Temp, TempUpdated }

class InteractiveWriterSpec extends AbstractBaseSpec with WritersTestUtils {

  val w = new InteractiveWriter(Ambient7Options())

  val co2 = Co2(1123)
  val temp = Temp(33.33)

  val co2NotSet = "co2: ----    "
  val co2Set = s"co2: ${Console.YELLOW}1123 ppm${Console.RESET}"

  val tempNotSet = "temp: -----   "
  val tempSet = s"temp: 33.33 °C"

  val div = "    "

  "InteractiveWriter" should "output empty screen on init" in {
    val (out, err) = checkWriterInit(w)
    err shouldBe ""
    out shouldBe s"\n ${Console.RED}ø${Console.RESET}  $co2NotSet$div$tempNotSet$div"
  }

  it should "output co2 without temp" in {
    checkWriterEvent(w, DeviceUp(time))
    val (out, err) = checkWriterEvent(w, Co2Updated(co2, time))
    err shouldBe ""
    out dropWhile (_ == '\b') shouldBe s"$div$co2Set$div$tempNotSet$div"
  }

  it should "output temp without co2" in {
    val w2 = new InteractiveWriter(Ambient7Options())
    checkWriterEvent(w2, DeviceUp(time))
    val (out, err) = checkWriterEvent(w2, TempUpdated(temp, time))
    err shouldBe ""
    out dropWhile (_ == '\b') shouldBe s"$div$co2NotSet$div$tempSet$div"
  }

  it should "output temp & co2" in {
    val (out, err) = checkWriterEvent(w, TempUpdated(temp, time + 1L))
    err shouldBe ""
    out dropWhile (_ == '\b') shouldBe s"$div$co2Set$div$tempSet$div"
  }


}
