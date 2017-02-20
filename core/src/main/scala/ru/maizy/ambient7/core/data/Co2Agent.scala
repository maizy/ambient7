package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

case class Co2Agent(agentName: String, tags: AgentTags) {
  val id: String = s"${agentName}__${tags.encoded}"
}
