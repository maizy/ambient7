package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.{ LocalDate, ZoneId, ZonedDateTime }
import java.time.format.DateTimeFormatter
import scala.util.{ Failure, Success, Try }
import org.scalatra.ScalatraBase


trait DateParamsSupport extends ScalatraBase with AppOptionsSupport {

  @throws(classOf[NoSuchElementException])
  @throws(classOf[IllegalArgumentException])
  def dateParam(key: String, timeZone: ZoneId = appOptions.timeZone): ZonedDateTime = {
    val raw = params(key)
    val tryDate = Try(DateTimeFormatter.ISO_LOCAL_DATE.parse(raw))
      .map(LocalDate.from(_).atStartOfDay(timeZone))

    tryDate match {
      case Success(d) => d
      case Failure(e) => throw new IllegalArgumentException("Unable to parse date \"" + raw + "\": " + e.getMessage)
    }
  }

  def optDateParam(key: String, timeZone: ZoneId = appOptions.timeZone): Option[ZonedDateTime] =
    Try(dateParam(key, timeZone)).toOption

}
