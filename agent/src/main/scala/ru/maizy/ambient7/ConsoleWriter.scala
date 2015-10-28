package ru.maizy.ambient7

import java.text.SimpleDateFormat
import java.util.{ Calendar, Date }

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class ConsoleWriter(opts: AppOptions) extends Writer {

  private def convertTimestamp(nanos: Long): String = {
    val millis = nanos / 1000000
    val date = new Date(millis)
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
  }

  override def write(event: Event): Unit = {
    event match {
      case Co2Updated(Co2(co2, _), ts) =>
        println(s"${convertTimestamp(ts)}: co2=$co2")
      case TempUpdated(Temp(temp), ts) =>
        println(f"${convertTimestamp(ts)}: temp=$temp%.2f")
      case DeviceUp(ts) =>
        System.err.println(f"${convertTimestamp(ts)}: device connected")
      case DeviceDown(ts) =>
        System.err.println(f"${convertTimestamp(ts)}: device disconnected")
    }
  }
}
