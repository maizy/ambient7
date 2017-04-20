package ru.maizy.ambient7.analysis.notifications.event

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import ru.maizy.ambient7.core.data.Co2Agent

abstract class AbstractCo2LevelEvent(
    val agent: Co2Agent,
    val currentValue: Int,
    val levelValue: Int,
    val time: ZonedDateTime
) extends Co2AgentEvent {

  private[event] def typeCode: String

  override def uid: String = s"co2_level_changed_${typeCode}_${agent.id}_${time.format(EVENT_TS_FORMAT)}"
}
