package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.{ LocalDate, ZoneId, ZonedDateTime }
import java.time.format.DateTimeFormatter
import scala.util.{ Failure, Success, Try }
import org.scalatra.ScalatraBase


trait DateParamsSupport extends ScalatraBase {

  @throws(classOf[NoSuchElementException])
  @throws(classOf[IllegalArgumentException])
  def dateParam(key: String): ZonedDateTime = {
    val raw = params(key)
    val tryDate = Try(DateTimeFormatter.ISO_LOCAL_DATE.parse(raw))
      .map(LocalDate.from(_).atStartOfDay(ZoneId.systemDefault()))

    tryDate match {
      case Success(d) => d
      case Failure(e) => throw new IllegalThreadStateException("Unable to parse date \"" + raw + "\": " + e.getMessage)
    }
  }

  def optDateParam(key: String): Option[ZonedDateTime] =
    Try(dateParam(key)).toOption

}
