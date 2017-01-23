package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */
trait Device {
  def deviceType: DeviceType.Type
  def id: String
}
