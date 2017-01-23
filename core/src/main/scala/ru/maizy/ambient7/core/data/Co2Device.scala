package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

case class Co2Device(id: String, agent: Co2Agent) extends Device {
  val deviceType = DeviceType.CO2
}
