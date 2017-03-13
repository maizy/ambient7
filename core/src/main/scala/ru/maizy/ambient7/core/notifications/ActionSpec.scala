package ru.maizy.ambient7.core.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

abstract sealed class ActionSpec {
  def id: String
  def actionType: ActionType.Type
}

case class SlackActionSpec(
    id: String,
    url: String,
    templates: ActionTemplates,
    channel: Option[String] = None,
    icon: Option[String] = None
) extends ActionSpec {
  val actionType = ActionType.Slack
}


case class StdoutActionSpec(
    id: String,
    templates: ActionTemplates
) extends ActionSpec {
  val actionType = ActionType.Stdout
}

case class InMemoryActionSpec(
    id: String,
    limit: Int = InMemoryActionSpec.DEFAULT_LIMIT
) extends ActionSpec {
  val actionType = ActionType.InMemory
}

object InMemoryActionSpec {
  final val DEFAULT_LIMIT = 1000
}
