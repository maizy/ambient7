package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import ru.maizy.ambient7.analysis.notifications.NotificationsExecutor
import ru.maizy.ambient7.core.config.Ambient7Options

object NotificationCommand {

  def run(opts: Ambient7Options): ReturnStatus = {
    println(opts.notifications.get)
    println(opts.devices.get)

    val executor = new NotificationsExecutor(opts)
    val executorFuture = executor.start()

    // normally never ends
    Await.ready(executorFuture, Duration.Inf)

    ReturnStatus.computeError
  }
}
