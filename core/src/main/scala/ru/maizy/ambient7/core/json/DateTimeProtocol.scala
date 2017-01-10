package ru.maizy.ambient7.core.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import spray.json.{ JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

trait DateTimeProtocol extends BaseProtocol
{
  implicit object ZonedDateTimeFormat extends RootJsonFormat[ZonedDateTime] {
    override def write(obj: ZonedDateTime): JsValue = {
      JsObject(
        "timestamp" -> JsNumber(obj.toEpochSecond),
        "local_iso8601" -> JsString(obj.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
        "year" -> JsNumber(obj.getYear),
        "month" -> JsNumber(obj.getMonthValue),
        "day" -> JsNumber(obj.getDayOfMonth),
        "hour" -> JsNumber(obj.getHour),
        "minute" -> JsNumber(obj.getMinute),
        "second" -> JsNumber(obj.getSecond),
        "weekday_iso8601" -> JsNumber(obj.getDayOfWeek.getValue),
        "zone" -> JsString(obj.getZone.getId)
      )
    }

    // TODO: implements
    override def read(json: JsValue): ZonedDateTime = ???
  }
}

object DateTimeProtocol extends DateTimeProtocol
