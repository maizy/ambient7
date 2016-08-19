package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import org.scalatra.ScalatraBase
import ru.maizy.ambient7.webapp.data.MT8057Device


trait DeviceParamSupport extends ScalatraBase {
  self: AppConfigSupport =>

  @throws(classOf[NoSuchElementException])
  @throws(classOf[IllegalArgumentException])
  def device(key: String = "device_id"): MT8057Device = {
    val deviceId = params("device_id")
    appConfig.mt8057Devices(deviceId)
  }

}
