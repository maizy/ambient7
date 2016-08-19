package ru.maizy.ambient7.core.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.{ ZoneId, ZonedDateTime }
import spray.json.{ JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

trait DateTimeProtocol extends BaseProtocol
{
  implicit object ZonedDateTimeFormat extends RootJsonFormat[ZonedDateTime] {
    override def write(obj: ZonedDateTime): JsValue = {
      val inSystemZone = obj.withZoneSameInstant(ZoneId.systemDefault())
      JsObject(
        "timestamp" -> JsNumber(inSystemZone.toEpochSecond),
        "local_iso8601" -> JsString(inSystemZone.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
        "year" -> JsNumber(inSystemZone.getYear),
        "month" -> JsNumber(inSystemZone.getMonthValue),
        "day" -> JsNumber(inSystemZone.getDayOfMonth),
        "hour" -> JsNumber(inSystemZone.getHour),
        "minute" -> JsNumber(inSystemZone.getMinute),
        "second" -> JsNumber(inSystemZone.getSecond),
        "weekday_iso8601" -> JsNumber(inSystemZone.getDayOfWeek.getValue),
        "zone" -> JsString(inSystemZone.getZone.getId)
      )
    }

    // TODO: implements
    override def read(json: JsValue): ZonedDateTime = ???
  }
}

object DateTimeProtocol extends DateTimeProtocol
