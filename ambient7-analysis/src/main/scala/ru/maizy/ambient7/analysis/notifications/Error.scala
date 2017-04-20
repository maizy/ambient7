package ru.maizy.ambient7.analysis.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

class Error(message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this(message, null)
}
