package ru.maizy.ambient7.core.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

case class ActionTemplates(
    co2LevelChangedIncreased: Option[String] = None,
    co2LevelChangedDecreased: Option[String] = None,
    available: Option[String] = None,
    unavailable: Option[String] = None
)
