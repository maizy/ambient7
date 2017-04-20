package ru.maizy.ambient7.analysis.notifications

import java.time.format.DateTimeFormatter

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

package object event {
  final val EVENT_TS_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss.S")
}
