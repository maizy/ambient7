package ru.maizy.ambient7.core.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import spray.json.{ JsNumber, JsObject, JsValue, RootJsonFormat, pimpAny }
import ru.maizy.ambient7.core.data.Co2AggregatedLevels

trait Co2AggregatedLevelsProtocol extends Co2AgentIdProtocol with DateTimeProtocol {

  implicit object Co2AggregatedLevelsFormat extends RootJsonFormat[Co2AggregatedLevels] {

    def write(value: Co2AggregatedLevels): JsValue = {
      val duration = java.time.Duration.between(value.from, value.to)
      JsObject(
        "low_level" -> JsNumber(value.lowLevel),
        "medium_level" -> JsNumber(value.mediumLevel),
        "high_level" -> JsNumber(value.highLevel),
        "unknown_level" -> JsNumber(value.unknownLevel),
        "total_secounds" -> JsNumber(duration.getSeconds),
        "agent" -> value.agentId.toJson,
        "from" -> value.from.toJson,
        "to" -> value.to.toJson
      )
    }

    // TODO: implements
    def read(json: JsValue): Co2AggregatedLevels = ???
  }
}

object Co2AggregatedLevelsProtocol extends Co2AggregatedLevelsProtocol
