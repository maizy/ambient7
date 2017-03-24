package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Ambient7Options

object NotificationCommand {

  def run(opts: Ambient7Options): ReturnStatus = {
    println(opts.notifications.get)
    println(opts.devices.get)
    ReturnStatus.success
  }
}
