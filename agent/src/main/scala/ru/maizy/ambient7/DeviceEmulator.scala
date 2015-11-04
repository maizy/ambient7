package ru.maizy.ambient7

import java.util.{Queue => JavaQueue}
import scala.util.Random
/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class DeviceEmulator(private val queue: JavaQueue[Event]) {

  def currentNanoTime(): Long = System.currentTimeMillis * 1000000

  def run(): Unit = {
    val randomGen = new Random()
    var enabled = true
    var lastCo2 = 450
    var lastTemp = 30.0
    queue.add(DeviceUp(currentNanoTime()))
    while(true) {
      val switch = if (enabled) {
        randomGen.nextInt(30) == 1
      } else {
        randomGen.nextInt(10) == 1
      }
      if (switch && enabled) {
        queue.add(DeviceDown(currentNanoTime()))
        enabled = false
      } else if (switch && !enabled) {
        queue.add(DeviceUp(currentNanoTime()))
        enabled = true
      } else {
        if (enabled) {
          if (randomGen.nextBoolean()) {
            val delta = randomGen.nextInt(100) - 50
            lastCo2 = Math.min(3000, Math.max(0, lastCo2 + delta))
            // TODO: high
            queue.add(Co2Updated(Co2(lastCo2), currentNanoTime()))
          } else {
            val delta = (randomGen.nextInt(200).toFloat / 100) - 1.0
            lastTemp = Math.min(50.0, Math.max(0.0, lastTemp + delta))
            queue.add(TempUpdated(Temp(lastTemp), currentNanoTime()))
          }
        }
        Thread.sleep(500 + randomGen.nextInt(500))
      }
    }
  }
}
