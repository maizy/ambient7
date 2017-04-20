package ru.maizy.ambient7.analysis.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import scala.concurrent.Future


trait Data {
  def update(now: ZonedDateTime = ZonedDateTime.now()): Future[Unit]
}
