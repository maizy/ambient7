package ru.maizy.ambient7

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class InteractiveWriter(opts: AppOptions) extends Writer {
  override def write(event: Event): Unit = {
    print(s"TODO (interactive writer): $event")
  }
}
