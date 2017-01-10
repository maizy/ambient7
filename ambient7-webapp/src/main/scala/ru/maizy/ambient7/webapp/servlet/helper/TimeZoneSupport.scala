package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import org.scalatra.ScalatraBase


trait TimeZoneSupport extends ScalatraBase with AppConfigSupport {
  implicit class ToAppTimeZoneOps(datetime: ZonedDateTime) {
    def toAppTimeZone: ZonedDateTime = {
      datetime.withZoneSameInstant(appConfig.timeZone)
    }
  }
}
