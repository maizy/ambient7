package ru.maizy.ambient7.core.config.options

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Defaults
import ru.maizy.ambient7.core.notifications.ActionSpec
import scala.concurrent.duration.Duration

case class NotificationsOptions(
    refreshRate: Duration = Defaults.NOTIFICATION_REFRESH_RATE,
    actionsSpecs: List[ActionSpec] = List.empty
) {
  val indexedActionsSpecs: Map[String, ActionSpec] = actionsSpecs.map(a => (a.id, a)).toMap

  def actionById(id: String): Option[ActionSpec] = indexedActionsSpecs.get(id)
}
