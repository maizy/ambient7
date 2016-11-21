package ru.maizy.ambient7.webapp.tests.servlet

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.{ LocalDateTime, ZoneOffset }
import org.json4s.{ JArray, JInt, JString, JValue }
import ru.maizy.ambient7.webapp.AppConfig
import ru.maizy.ambient7.webapp.servlet.Co2ReportServlet
import ru.maizy.ambient7.webapp.tests.{ BaseServletAndDbTest, JsonAsserts }


class Co2ReportServletSpec extends BaseServletAndDbTest with JsonAsserts {

  case class ExpectedItem(from: String, low: Int, medium: Int, high: Int, unknown: Int)

  override def initServlets(config: AppConfig): Unit = {
    addServlet(new Co2ReportServlet(config), "/co2_report")
  }

  override def setupConfig(config: AppConfig): Unit = {
    val newConfig = config.copy(timeZone = ZoneOffset.UTC)
    bootstrap.overwriteAppConfig(newConfig)
  }

  "/co2_report/by_day" should "return error for unknown devied id" in {
    get("/co2_report/by_day?from=2001-01-01&days=1&device_id=unknown") {
      status shouldBe 400
    }
  }

  it should "return empty report for days without data" in {
    get("/co2_report/by_day?from=2015-01-01&days=1&device_id=one") {
      status shouldBe 200
      assertReportItems(parseJson(body), IndexedSeq(ExpectedItem("2015-01-01T00:00:00", 0, 0, 0, 1440)))
    }
  }

  it should "return result for one day" in {
    // device_id=one, agent_id = main
    loadSql("co2-report-one-day.sql", Map("day" -> "2016-10-01", "agent" -> "main"))
    get("/co2_report/by_day?from=2016-10-01&days=1&device_id=one") {
      status shouldBe 200
      val resJson = parseJson(body)
      assertJson(resJson \\ "duration") {
        """{
          "hours": 24,
          "days": 1
        }"""
      }
      assertReportItems(
        resJson,
        IndexedSeq(
          ExpectedItem("2016-10-01T00:00:00", 480, 360, 260, 340)
        )
      )
    }
  }

  it should "support ?to argument" in {
    loadSql("co2-report-one-day.sql", Map("day" -> "2016-03-02", "agent" -> "main"))
    get("/co2_report/by_day?from=2016-03-01&to=2016-03-04&device_id=one") {
      status shouldBe 200
      assertReportItems(
        parseJson(body),
        IndexedSeq(
          ExpectedItem("2016-03-01T00:00:00", 0, 0, 0, 1440),
          ExpectedItem("2016-03-02T00:00:00", 480, 360, 260, 340),
          ExpectedItem("2016-03-03T00:00:00", 0, 0, 0, 1440)
        )
      )
    }
  }

  it should "return result for one day with data & two unknown days" in {
    loadSql("co2-report-one-day.sql", Map("day" -> "2016-02-02", "agent" -> "main"))
    get("/co2_report/by_day?from=2016-02-01&days=3&device_id=one") {
      status shouldBe 200
      assertReportItems(
        parseJson(body),
        IndexedSeq(
          ExpectedItem("2016-02-01T00:00:00", 0, 0, 0, 1440),
          ExpectedItem("2016-02-02T00:00:00", 480, 360, 260, 340),
          ExpectedItem("2016-02-03T00:00:00", 0, 0, 0, 1440)
        )
      )
    }
  }

  "/co2_report/by_hour" should "return error for unknown device id" in {
    get("/co2_report/by_day?from=2012-01-01&days=1&device_id=unknown") {
      status shouldBe 400
    }
  }

  it should "return result for one day with data plus one unknown day" in {
    loadSql("co2-report-one-day.sql", Map("day" -> "2016-05-02", "agent" -> "main"))
    get("/co2_report/by_hour?from=2016-05-01&days=2&device_id=one") {
      status shouldBe 200
      val resJson = parseJson(body)
      assertJson(resJson \\ "duration") {
        """{
          "hours": 48,
          "days": 2
        }"""
      }
      assertReportItems(
        resJson,
        IndexedSeq(
          ExpectedItem("2016-05-01T00:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T01:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T02:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T03:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T04:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T05:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T06:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T07:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T08:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T09:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T10:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T11:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T12:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T13:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T14:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T15:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T16:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T17:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T18:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T19:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T20:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T21:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T22:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-01T23:00:00", 0, 0, 0, 60),

          ExpectedItem("2016-05-02T00:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-02T01:00:00", 0, 10, 10, 40),
          ExpectedItem("2016-05-02T02:00:00", 0, 30, 30, 0),
          ExpectedItem("2016-05-02T03:00:00", 40, 20, 0, 0),
          ExpectedItem("2016-05-02T04:00:00", 40, 0, 20, 0),
          ExpectedItem("2016-05-02T05:00:00", 20, 20, 10, 10),
          ExpectedItem("2016-05-02T06:00:00", 30, 0, 0, 30),
          ExpectedItem("2016-05-02T07:00:00", 0, 30, 0, 30),
          ExpectedItem("2016-05-02T08:00:00", 50, 10, 0, 0),
          ExpectedItem("2016-05-02T09:00:00", 60, 0, 0, 0),
          ExpectedItem("2016-05-02T10:00:00", 0, 60, 0, 0),
          ExpectedItem("2016-05-02T11:00:00", 0, 0, 60, 0),
          ExpectedItem("2016-05-02T12:00:00", 0, 0, 0, 60),
          ExpectedItem("2016-05-02T13:00:00", 0, 10, 10, 40),
          ExpectedItem("2016-05-02T14:00:00", 0, 30, 30, 0),
          ExpectedItem("2016-05-02T15:00:00", 40, 20, 0, 0),
          ExpectedItem("2016-05-02T16:00:00", 40, 0, 20, 0),
          ExpectedItem("2016-05-02T17:00:00", 20, 20, 10, 10),
          ExpectedItem("2016-05-02T18:00:00", 30, 0, 0, 30),
          ExpectedItem("2016-05-02T19:00:00", 0, 30, 0, 30),
          ExpectedItem("2016-05-02T20:00:00", 50, 10, 0, 0),
          ExpectedItem("2016-05-02T21:00:00", 60, 0, 0, 0),
          ExpectedItem("2016-05-02T22:00:00", 0, 60, 0, 0),
          ExpectedItem("2016-05-02T23:00:00", 0, 0, 60, 0)
        )
      )
    }
  }

  def assertReportItems(json: JValue, expected: IndexedSeq[ExpectedItem]): Unit = {
    val items = (json \\ "items").asInstanceOf[JArray]
    items.values should have size expected.size.toLong
    for ((expectedItem, index) <- expected.zipWithIndex) {
      assertReportItem(items(index), expectedItem)
    }
  }

  def assertReportItem(item: JValue, expected: ExpectedItem): Unit = {
    val expectedFrom = LocalDateTime.from(
      DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(expected.from)
    ).atZone(bootstrap.appConfig.timeZone)

    (item \\ "low_level") shouldBe JInt(expected.low)
    (item \\ "medium_level") shouldBe JInt(expected.medium)
    (item \\ "high_level") shouldBe JInt(expected.high)
    (item \\ "unknown_level") shouldBe JInt(expected.unknown)
    (item \\ "from" \\ "local_iso8601") shouldBe JString(expectedFrom.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    (item \\ "from" \\ "zone") shouldBe JString(expectedFrom.getZone.getId)
  }
}
