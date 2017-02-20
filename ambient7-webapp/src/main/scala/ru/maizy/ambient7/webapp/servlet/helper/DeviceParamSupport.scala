package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import org.scalatra.ScalatraBase
import ru.maizy.ambient7.core.data.Co2Device


trait DeviceParamSupport extends ScalatraBase {
  self: AppOptionsSupport =>

  @throws(classOf[NoSuchElementException])
  @throws(classOf[IllegalArgumentException])
  def device(key: String = "device_id"): Co2Device = {
    val deviceId = params("device_id")
    val co2Devices = appOptions.devices.map(_.co2Devices).getOrElse(List.empty)
    co2Devices.find(_.id == deviceId) match {
      case None => throw new NoSuchElementException("unknown device_id")
      case Some(d) => d
    }
  }

}
