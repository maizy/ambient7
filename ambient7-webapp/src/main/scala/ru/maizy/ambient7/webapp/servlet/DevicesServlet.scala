package ru.maizy.ambient7.webapp.servlet

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import spray.json.{ JsObject, pimpAny }
import ru.maizy.ambient7.webapp.servlet.error.IllegalPathParam
import ru.maizy.ambient7.webapp.servlet.helper.SprayJsonSupport
import ru.maizy.ambient7.webapp.{ Ambient7WebAppStack, AppConfig }

class DevicesServlet(appConfig: AppConfig)
  extends Ambient7WebAppStack
  with SprayJsonSupport
{

  get("/") {
    import ru.maizy.ambient7.webapp.json.Co2DeviceProtocol._
    JsObject(
      "co2" -> appConfig.co2Devices.values.toJson
    )
  }

  get("/:deviceId") {
    import ru.maizy.ambient7.webapp.json.Co2DeviceProtocol
    implicit val deviceFormat = Co2DeviceProtocol.deviceFormat
    val deviceId = params("deviceId")
    appConfig.co2Devices.get(deviceId) match {
      case Some(device) => device.toJson
      case None => throw new IllegalPathParam("unknown device")
    }
  }
}
