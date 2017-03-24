package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.notifications.WatcherSpec

case class Co2Device(id: String, agent: Co2Agent, watchersSpecs: List[WatcherSpec] = List.empty)
  extends Device
{
  val deviceType = DeviceType.CO2
}
