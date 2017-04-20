package ru.maizy.ambient7.analysis.notifications.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import ru.maizy.ambient7.analysis.notifications.data.{ Co2AgentData, Data }
import ru.maizy.ambient7.analysis.notifications.event.Co2LevelDecreasedEvent

class Co2LevelWatcher extends Co2AgentWatcher {
  override def processData(data: Data): Unit = data match {
    case co2Data: Co2AgentData =>

      // FIXME: tmp
      val time = ZonedDateTime.now()
      val event = new Co2LevelDecreasedEvent(co2Data.agent, 889, 800, time)
      logger.debug(s"publish event: ${event.getClass.getCanonicalName}")
      publish(event)
      ()
      // TODO: helper: window size + notify after + resolve after
    case _ => logger.warn(s"unexpected data type {${data.getClass}}")
  }
}
