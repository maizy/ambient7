package ru.maizy.ambient7.mt8057agent.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.mt8057agent.{ StatefullWriter, Co2, Temp }
import ru.maizy.ambient7.mt8057agent.{ DeviceDown, DeviceUp, TempUpdated, Co2Updated, Event }

class StatefullWriterSpec extends AbstractBaseSpec with WritersTestUtils {

  class TestWriter extends StatefullWriter {
    var lastEvent: Option[Event] = None
    override protected def internalWrite(event: Event): Unit = lastEvent = Some(event)

    override def onInit(): Unit = {}

    def connectedTest: Boolean = connected
    def co2Test: Option[Co2] = co2
    def tempTest: Option[Temp] = temp
    def lastStateUpdateTest: Option[Long] = lastStateUpdate
  }

  val w = new TestWriter
  w.onInit()

  "StatefullWriter" should "init in proper state" in {
    w.connectedTest shouldBe false
    w.co2Test should be ('empty)
    w.tempTest should be ('empty)
  }

  it should "set connected state after DeviceUp event and ignore next DeviceUp event" in {
    w.write(DeviceUp(time))
    w.connectedTest shouldBe true
    w.lastStateUpdateTest should be (Some(time))
    w.write(DeviceUp(time + 2L))
    w.lastStateUpdateTest should be (Some(time))
  }

  it should "unset connect state after DeviceDown and ignore next DeviceDown event" in {
    w.write(DeviceDown(time + 3L))
    w.connectedTest shouldBe false
    w.lastStateUpdateTest should be (Some(time + 3L))
    w.write(DeviceDown(time + 4L))
    w.lastStateUpdateTest should be (Some(time + 3L))
  }

  it should "update co2" in {
    w.write(Co2Updated(Co2(777), time + 5L))
    w.co2Test should be (Some(Co2(777)))
  }

  it should "update temp" in {
    w.write(TempUpdated(Temp(33.3), time + 6L))
    w.tempTest should be (Some(Temp(33.3)))
  }

}
