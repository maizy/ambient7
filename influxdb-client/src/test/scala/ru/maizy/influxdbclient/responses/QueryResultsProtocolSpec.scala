package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import spray.json._
import ru.maizy.influxdbclient.BaseSpec
import ru.maizy.influxdbclient.data.{ Column, NumberValue, QueryResult, Series, SeriesError, SeriesItem, StringValue }

class QueryResultsProtocolSpec extends BaseSpec {

  import ru.maizy.influxdbclient.responses.QueryResultsProtocol._

  "QueryResultsProtocol" should "deserialize single column with string" in {

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
    // scalastyle:on

    val res = JsonParser(ParserInput(singleColumn)).convertTo[QueryResult]
    val expected = QueryResult(
      IndexedSeq(Right(Series(
        IndexedSeq(SeriesItem(
          name = "measurements",
          columns = IndexedSeq(Column("name")),
          values = IndexedSeq(IndexedSeq(StringValue("co2")), IndexedSeq(StringValue("temp")))
        ))
      )))
    )

    res shouldBe expected
  }

  it should "deserialize two columns with string & number" in {
    // scalastyle:off
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

    val res = JsonParser(ParserInput(twoColumnsWithStringAndNumber)).convertTo[QueryResult]
    val expected = QueryResult(
      IndexedSeq(Right(Series(
        IndexedSeq(SeriesItem(
          name = "co2",
          columns = IndexedSeq(Column("time"), Column("max")),
          values = IndexedSeq(
            IndexedSeq(StringValue("2015-11-06T00:00:00Z"), NumberValue(802)),
            IndexedSeq(StringValue("2015-11-06T00:01:00Z"), NumberValue(802)),
            IndexedSeq(StringValue("2015-11-06T00:02:00Z"), NumberValue(791))
          )
        ))
      )))
    )

    res shouldBe expected
  }

  it should "deserialize emply result" in {
    val body = """{"results":[{}]}"""
    val res = JsonParser(ParserInput(body)).convertTo[QueryResult]
    val expected = QueryResult(IndexedSeq(Right(Series(IndexedSeq.empty))))

    res shouldBe expected
  }

  it should "deserialize no results" in {
    val body = """{}"""
    val res = JsonParser(ParserInput(body)).convertTo[QueryResult]
    val expected = QueryResult(IndexedSeq.empty)

    res shouldBe expected
  }

  it should "deserialize results with error in some subquery" in {

    // scalastyle:off
    val body = """{
      "results": [
        {
          "error": "too many points in the group by interval"
        },
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
    // scalastyle:on

    val res = JsonParser(ParserInput(body)).convertTo[QueryResult]
    val expected = QueryResult(
      IndexedSeq(
        Left(SeriesError("too many points in the group by interval")),
        Right(Series(IndexedSeq(SeriesItem(
          name = "measurements",
          columns = IndexedSeq(Column("name")),
          values = IndexedSeq(IndexedSeq(StringValue("co2")), IndexedSeq(StringValue("temp")))
        ))))
      )
    )

    res shouldBe expected


  }

}
