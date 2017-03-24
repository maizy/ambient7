package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.notifications.WatcherSpec

trait Device {
  def deviceType: DeviceType.Type
  def id: String
  def watchersSpecs: List[WatcherSpec]
}
