package ru.maizy.ambient7.core.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.Duration

abstract sealed class WatcherSpec {
  def watcherType: WatcherType.Type
  def actionsIds: List[String]
}

case class Co2LevelChangedWatcherSpec(
    actionsIds: List[String],
    notifyAfter: Option[Duration] = None,
    resolveAfter: Option[Duration] = None
  ) extends WatcherSpec
{
  val watcherType = WatcherType.Co2LevelChanged
}

case class AvailabilityWatcherSpec(
    actionsIds: List[String],
    notifyAfter: Option[Duration] = None,
    resolveAfter: Option[Duration] = None
  ) extends WatcherSpec
{
  val watcherType = WatcherType.Availability
}
