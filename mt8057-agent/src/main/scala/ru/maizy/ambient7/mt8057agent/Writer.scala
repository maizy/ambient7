package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */
trait Writer {
  def onInit(): Unit
  def write(event: Event): Unit
}
