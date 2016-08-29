package ru.maizy.ambient7.webapp.servlet

import java.time.ZonedDateTime
import scalikejdbc._
import spray.json.{ JsNumber, JsObject, JsString, pimpAny }
import ru.maizy.ambient7.rdbms.Co2Service
import ru.maizy.ambient7.webapp.data.MT8057Device
import ru.maizy.ambient7.webapp.servlet.helper.{ AppConfigSupport, DateParamsSupport, DeviceParamSupport }
import ru.maizy.ambient7.webapp.servlet.helper.{ PrimitiveParamsSupport, SprayJsonSupport }
import ru.maizy.ambient7.webapp.{ Ambient7WebAppStack, AppConfig }

class Co2ReportServlet(val appConfig: AppConfig)
  extends Ambient7WebAppStack
  with AppConfigSupport
  with DateParamsSupport
  with PrimitiveParamsSupport
  with DeviceParamSupport
  with SprayJsonSupport
{

  import ru.maizy.ambient7.core.json.Co2AggregatedLevelsProtocol._

  get("/by_hour") {
    val (from, to, device) = getReportParams
    val aggregates = DB readOnly { implicit session =>
      Co2Service.getHourlyAggregates(
        device.agentId,
        from,
        to
      )
    }

    JsObject(
      "items" -> aggregates.toJson,
      "by" -> JsString("hour"),
      "from" -> from.toJson,
      "to" -> to.toJson,
      "hours_total" -> JsNumber(java.time.Duration.between(from, to).toHours)
    )
  }

  get("/by_day") {
    val (from, to, device) = getReportParams
    val aggregates = DB readOnly { implicit session =>
      Co2Service.computeDailyAggregates(
        device.agentId,
        from,
        to
      )
    }

    JsObject(
      "items" -> aggregates.toJson,
      "by" -> JsString("day"),
      "from" -> from.toJson,
      "to" -> to.toJson,
      "days_total" -> JsNumber(java.time.Duration.between(from, to).toDays)
    )
  }

  protected def getReportParams: (ZonedDateTime, ZonedDateTime, MT8057Device) = {
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
    (from, to, forDevice)
  }
}