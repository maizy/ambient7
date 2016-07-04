package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.influxdbclient.dto.ErrorDto
import spray.json.{ JsObject, JsString, JsValue, RootJsonFormat, deserializationError }


trait ErrorProtocol extends BaseProtocol {
  implicit object ErrorFormat extends RootJsonFormat[ErrorDto] {

    def write(value: ErrorDto): JsValue = JsObject(
      "error" -> JsString(value.message)
    )

    def read(json: JsValue): ErrorDto = json.asJsObject.getFields("error") match {
      case Seq(JsString(message)) => ErrorDto(message)
      case _ => deserializationError("expect error with error field")
    }
  }
}

object ErrorProtocol extends ErrorProtocol

// ex:
//  {
//     "error": "error parsing query: found ..."
//  }

