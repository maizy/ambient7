package ru.maizy.ambient7.webapp.servlet

import spray.json.{ JsObject, pimpAny }
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
}
