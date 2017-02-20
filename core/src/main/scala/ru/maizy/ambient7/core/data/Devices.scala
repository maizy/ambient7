package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

case class Devices(co2Devices: List[Co2Device] = List.empty) {
  def allDevices: List[Device] = co2Devices
}
