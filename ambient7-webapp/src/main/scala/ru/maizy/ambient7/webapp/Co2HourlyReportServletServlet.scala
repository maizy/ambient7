package ru.maizy.ambient7.webapp

import scalikejdbc._
import ru.maizy.ambient7.webapp.servlet.helper.{ DateParamsSupport, PrimitiveParamsSupport }

class Co2HourlyReportServletServlet(config: AppConfig)
  extends Ambient7WebAppStack
  with DateParamsSupport
  with PrimitiveParamsSupport
{

  val BASE = "/co2_hourly_report"

  get(s"$BASE/by_date") {
    var tmp = ""
    // FIXME: tmp
    DB readOnly { implicit session =>
      val res = sql"""select count(1) as res from "co2_hourly_report"""".map(r => r.int("res")).single.apply()
      tmp = tmp + res.toString
    }

    val from = dateParam("from")
    val mayBeTo = optDateParam("to") orElse optIntParam("days").map{ d => from.plusDays(d.toLong) }
    mayBeTo match {
      case None =>
        throw new IllegalArgumentException("to or duration param should be specified")
      case Some(to) if to.compareTo(from) <= 0 =>
        throw new IllegalArgumentException("wrong period")
      case Some(to) =>
        s"from: $from, to: $to\n$tmp"
    }
  }
}
