package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.influxdbclient.data.{ Column, NullValue, NumberValue, QueryResult, Series, SeriesError, SeriesItem }
import ru.maizy.influxdbclient.data.{ StringValue, Value }
import spray.json.{ JsArray, JsNull, JsNumber, JsObject, JsString, JsValue, RootJsonFormat, deserializationError }

object QueryResultsProtocol extends ErrorProtocol {

  implicit object ValueFormat extends RootJsonFormat[Value] {

    def write(value: Value): JsValue = ???  // TODO: implements

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
      case _ => deserializationError("column \"name\" expected to be a json string")
    }
  }

  implicit val seriestItemFormat = jsonFormat3(SeriesItem)
  implicit val seriestErrorFormat = jsonFormat1(SeriesError)

  implicit object ResultFormat extends RootJsonFormat[Series] {

    def write(column: Series): JsValue = ???

    def read(json: JsValue): Series = json match {
      case JsObject.empty => Series(IndexedSeq.empty)
      case o: JsObject => o.getFields("series") match {
        case Seq(r: JsArray) => Series(r.convertTo[IndexedSeq[SeriesItem]])
        case _ => deserializationError("column \"series\" expected to be an array")
      }
      case _ => deserializationError("column \"series\" not found")
    }
  }

  implicit object QueryResultFormat extends RootJsonFormat[QueryResult] {

    def write(column: QueryResult): JsValue = ???

    def read(json: JsValue): QueryResult = json match {
      case JsObject.empty => QueryResult(IndexedSeq.empty)
      case o: JsObject => o.getFields("results") match {
        case Seq(r: JsArray) =>
          val results = r.elements.map {
            case o: JsObject if o.fields.contains("error") => Left(o.convertTo[SeriesError])
            case o: JsObject => Right(o.convertTo[Series])
            case _ => deserializationError("unable to detect series result (expected value or error)")
          }
          QueryResult(results)
        case _ => deserializationError("column \"results\" expected to be an array")
      }
      case _ => deserializationError("column \"results\" not found")
    }
  }

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
