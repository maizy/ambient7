package ru.maizy.ambient7.analysis.notifications.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.collection.mutable
import ru.maizy.ambient7.analysis.notifications.data.Data
import ru.maizy.ambient7.analysis.notifications.event.Event

trait Watcher extends mutable.Publisher[Event] {

  def processData(data: Data): Unit
}
