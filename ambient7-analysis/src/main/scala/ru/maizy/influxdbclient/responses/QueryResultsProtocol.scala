package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.influxdbclient.dto.{ Column, NullValue, NumberValue, QueryResult, Result, SeriesItem, StringValue }
import ru.maizy.influxdbclient.dto.Value
import spray.json.{ JsNull, JsNumber, JsString, JsValue, RootJsonFormat, deserializationError }

object QueryResultsProtocol extends ErrorProtocol {

  implicit object ValueFormat extends RootJsonFormat[Value] {

    def write(value: Value): JsValue = ???  // FIXME: implements

    def read(json: JsValue): Value = json match {
      case JsString(name) => new StringValue(name)
      case JsNumber(num) => new NumberValue(num)
      case JsNull => NullValue
      case _ => deserializationError("unknown result value")
    }
  }

  implicit object ColumnFormat extends RootJsonFormat[Column] {

    def write(column: Column): JsValue = JsString(column.name)

    def read(json: JsValue): Column = json match {
      case JsString(name) => Column(name)
      case _ => deserializationError("column name expected to be a json string")
    }
  }

  implicit val seriestItemFormat = jsonFormat3(SeriesItem)
  implicit val resultFormat = jsonFormat1(Result)
  implicit val queryResultFormat = jsonFormat1(QueryResult)
}

// ex.
//  {
//    "results": [
//      {
//        "series": [
//          {
//            "name": "co2",
//            "columns": [
//              "time",
//              "max"
//            ],
//            "values": [
//              [
//                "2015-11-06T00:00:00Z",
//                802
//              ]
//            ]
//          }
//        ]
//      }
//    ]
//  }
