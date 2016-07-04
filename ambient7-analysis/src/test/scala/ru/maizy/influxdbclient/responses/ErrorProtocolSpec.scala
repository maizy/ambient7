package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import spray.json._
import ru.maizy.influxdbclient.BaseSpec
import ru.maizy.influxdbclient.dto.ErrorDto

class ErrorProtocolSpec extends BaseSpec {

  import ru.maizy.influxdbclient.responses.ErrorProtocol._

  "ErrorProtocol" should "deserialize error" in {
    val body = """{"error": "error parsing query: found ..."}"""
    JsonParser(ParserInput(body)).convertTo[ErrorDto] shouldBe ErrorDto("error parsing query: found ...")
  }

  it should "serialize error" in {
    ErrorDto("abc").toJson.compactPrint shouldBe """{"error":"abc"}"""
  }
}
