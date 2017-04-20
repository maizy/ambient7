package ru.maizy.ambient7.core.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

object ActionType extends Enumeration {
  type Type = Value
  val Stdout, InMemory, Slack = Value
}
