package ru.maizy.ambient7.analysis.notifications.event

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import ru.maizy.ambient7.core.data.Co2Agent

class Co2LevelDecreasedEvent(
    agent: Co2Agent,
    currentValue: Int,
    levelValue: Int,
    time: ZonedDateTime
) extends AbstractCo2LevelEvent(agent, currentValue, levelValue, time) {
  override private[event] def typeCode = "decreased"
}
