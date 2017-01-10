package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import org.scalatra.ScalatraBase
import ru.maizy.ambient7.webapp.data.Co2Device


trait DeviceParamSupport extends ScalatraBase {
  self: AppConfigSupport =>

  @throws(classOf[NoSuchElementException])
  @throws(classOf[IllegalArgumentException])
  def device(key: String = "device_id"): Co2Device = {
    val deviceId = params("device_id")
    appConfig.co2Devices(deviceId)
  }

}
