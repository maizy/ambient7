package ru.maizy.ambient7.webapp.servlet

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import spray.json.{ JsObject, pimpAny }
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.core.data.Co2Device
import ru.maizy.ambient7.webapp.servlet.error.IllegalPathParam
import ru.maizy.ambient7.webapp.servlet.helper.SprayJsonSupport
import ru.maizy.ambient7.webapp.Ambient7WebAppStack

class DevicesServlet(val appConfig: Ambient7Options)
  extends Ambient7WebAppStack
  with SprayJsonSupport
{

  def co2Devices: List[Co2Device] = appConfig.devices.map(_.co2Devices).getOrElse(List.empty)

  get("/") {
    import ru.maizy.ambient7.webapp.json.Co2DeviceProtocol._

    JsObject(
      "co2" -> co2Devices.toJson
    )
  }

  get("/:deviceId") {
    import ru.maizy.ambient7.webapp.json.Co2DeviceProtocol
    implicit val deviceFormat = Co2DeviceProtocol.deviceFormat
    val deviceId = params("deviceId")
    co2Devices.find(_.id == deviceId) match {
      case Some(device) => device.toJson
      case None => throw new IllegalPathParam("unknown device")
    }
  }
}
