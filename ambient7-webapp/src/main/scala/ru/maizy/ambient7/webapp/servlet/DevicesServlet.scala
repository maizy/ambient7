package ru.maizy.ambient7.webapp.servlet

import spray.json.{ JsObject, pimpAny }
import ru.maizy.ambient7.webapp.servlet.helper.SprayJsonSupport
import ru.maizy.ambient7.webapp.{ Ambient7WebAppStack, AppConfig }

class DevicesServlet(appConfig: AppConfig)
  extends Ambient7WebAppStack
  with SprayJsonSupport
{

  get("/") {
    import ru.maizy.ambient7.webapp.json.MT8057DeviceProtocol._
    JsObject(
      "mt8057" -> appConfig.mt8057Devices.values.toJson
    )
  }
}
