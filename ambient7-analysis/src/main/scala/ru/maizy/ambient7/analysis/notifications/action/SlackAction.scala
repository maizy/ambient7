package ru.maizy.ambient7.analysis.notifications.action

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.collection.mutable
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.ambient7.analysis.notifications.event.Event

class SlackAction extends Action with LazyLogging {
  override def notify(pub: mutable.Publisher[Event], event: Event): Unit = {
    logger.debug(s"receive event: ${event.getClass.getName}")
  }
}
