package ru.maizy.ambient7.webapp.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import spray.json.{ JsObject, JsString, JsValue, RootJsonFormat, pimpAny }
import ru.maizy.ambient7.core.data.Co2Device
import ru.maizy.ambient7.core.json.{ BaseProtocol, Co2AgentProtocol }

trait Co2DeviceProtocol extends BaseProtocol with Co2AgentProtocol {

  implicit object Co2DeviceFormat extends RootJsonFormat[Co2Device] {

    def write(value: Co2Device): JsValue = {
      // TODO: convert to json watchers specs
      JsObject(
        "id" -> JsString(value.id),
        "agent" -> value.agent.toJson
      )
    }

    // TODO: implements
    def read(json: JsValue): Co2Device = ???
  }
}

object Co2DeviceProtocol extends Co2DeviceProtocol
