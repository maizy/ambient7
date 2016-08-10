package ru.maizy.ambient7.webapp

import ru.maizy.ambient7.webapp.servlet.helper.{ DatesSupport, PrimitivesSupport }

class Co2HourlyReportServletServle extends Ambient7WebAppStack with DatesSupport with PrimitivesSupport
{

  val BASE = "/co2_hourly_report"

  get(s"$BASE/by_date") {
    val from = dateParam("from")
    val mayBeTo = optDateParam("to") orElse optIntParam("days").map{ d => from.plusDays(d.toLong) }
    mayBeTo match {
      case None =>
        throw new IllegalArgumentException("to or duration param should be specified")
      case Some(to) if to.compareTo(from) <= 0 =>
        throw new IllegalArgumentException("wrong period")
      case Some(to) =>
        s"from: $from, to: $to"
    }
  }
}
