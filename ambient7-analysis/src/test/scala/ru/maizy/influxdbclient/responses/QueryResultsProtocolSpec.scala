package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import spray.json._
import ru.maizy.influxdbclient.BaseSpec
import ru.maizy.influxdbclient.dto.{ Column, NumberValue, QueryResult, Result, SeriesItem, StringValue }

class QueryResultsProtocolSpec extends BaseSpec {

  import ru.maizy.influxdbclient.responses.QueryResultsProtocol._

  // scalastyle:off
  val singleColumn = """{
      "results": [
        {
          "series": [
            {
              "name": "measurements",
              "columns": [
                "name"
              ],
              "values": [
                [
                  "co2"
                ],
                [
                  "temp"
                ]
              ]
            }
          ]
        }
      ]
    }
    """


  val twoColumnsWithStringAndNumber = """{
      "results": [
        {
          "series": [
            {
              "name": "co2",
              "columns": [
                "time",
                "max"
              ],
              "values": [
                [
                  "2015-11-06T00:00:00Z",
                  802
                ],
                [
                  "2015-11-06T00:01:00Z",
                  802
                ],
                [
                  "2015-11-06T00:02:00Z",
                  791
                ]
              ]
            }
          ]
        }
      ]
    }
    """
  // scalastyle:on

  "QueryResultsProtocol" should "deserialize single column with string" in {

    val res = JsonParser(ParserInput(singleColumn)).convertTo[QueryResult]
    val expected = QueryResult(
      IndexedSeq(Result(
        IndexedSeq(SeriesItem(
          name = "measurements",
          columns = IndexedSeq(Column("name")),
          values = IndexedSeq(IndexedSeq(StringValue("co2")), IndexedSeq(StringValue("temp")))
        ))
      ))
    )

    res shouldBe expected
  }

  it should "deserialize two columns with string & number" in {
    val res = JsonParser(ParserInput(twoColumnsWithStringAndNumber)).convertTo[QueryResult]
    val expected = QueryResult(
      IndexedSeq(Result(
        IndexedSeq(SeriesItem(
          name = "co2",
          columns = IndexedSeq(Column("time"), Column("max")),
          values = IndexedSeq(
            IndexedSeq(StringValue("2015-11-06T00:00:00Z"), NumberValue(802)),
            IndexedSeq(StringValue("2015-11-06T00:01:00Z"), NumberValue(802)),
            IndexedSeq(StringValue("2015-11-06T00:02:00Z"), NumberValue(791))
          )
        ))
      ))
    )
  }

}
