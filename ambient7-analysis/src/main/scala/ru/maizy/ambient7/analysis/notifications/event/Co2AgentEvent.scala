package ru.maizy.ambient7.analysis.notifications.event

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.data.Co2Agent

trait Co2AgentEvent extends Event {
  def agent: Co2Agent
  def deviceId: String = agent.id

  // TODO
  def deviceDescription: String = deviceId
}
