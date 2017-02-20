package ru.maizy.ambient7.webapp.servlet

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import scalikejdbc._
import spray.json.{ JsNumber, JsObject, JsString, JsValue, pimpAny }
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.core.data.Co2Device
import ru.maizy.ambient7.rdbms.Co2Service
import ru.maizy.ambient7.webapp.servlet.helper.{ AppOptionsSupport, DateParamsSupport, DeviceParamSupport }
import ru.maizy.ambient7.webapp.servlet.helper.{ PrimitiveParamsSupport, SprayJsonSupport, TimeZoneSupport }
import ru.maizy.ambient7.webapp.Ambient7WebAppStack

class Co2ReportServlet(val appOptions: Ambient7Options)
  extends Ambient7WebAppStack
  with AppOptionsSupport
  with DateParamsSupport
  with PrimitiveParamsSupport
  with DeviceParamSupport
  with SprayJsonSupport
  with TimeZoneSupport
{

  import ru.maizy.ambient7.core.json.Co2AggregatedLevelsProtocol._

  get("/by_hour") {
    val (from, to, device) = getReportParams
    val aggregates = DB readOnly { implicit session =>
      Co2Service.getHourlyAggregates(
        device.agent,
        from,
        to
      )
    }
    reportResult(aggregates.toJson, from, to, by="day")
  }

  get("/by_day") {
    val (from, to, device) = getReportParams
    val aggregates = DB readOnly { implicit session =>
      Co2Service.computeDailyAggregates(
        device.agent,
        from,
        to
      )
    }
    reportResult(aggregates.toJson, from, to, by="day")
  }

  protected def reportResult(items: JsValue, from: ZonedDateTime, to: ZonedDateTime, by: String): JsObject = {
    JsObject(
      "items" -> items,
      "by" -> JsString(by),
      "from" -> from.toJson,
      "to" -> to.toJson,
      "duration" -> JsObject(
        "hours" -> JsNumber(java.time.Duration.between(from, to).toHours),
        "days" -> JsNumber(java.time.Duration.between(from, to).toDays)
      )
    )
  }

  protected def getReportParams: (ZonedDateTime, ZonedDateTime, Co2Device) = {
    val from = dateParam("from")
    val forDevice = device()
    val mayBeTo = optDateParam("to") orElse optIntParam("days").map { d => from.plusDays(d.toLong) }
    val to = mayBeTo match {
      case None =>
        throw new IllegalArgumentException("to or duration param should be specified")
      case Some(t) if t.compareTo(from) <= 0 =>
        throw new IllegalArgumentException("wrong period")
      case Some(t) => t
    }
    (from.toAppTimeZone, to.toAppTimeZone, forDevice)
  }
}
