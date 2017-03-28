package ru.maizy.ambient7.analysis.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.Future
import ru.maizy.ambient7.core.config.Ambient7Options


class NotificationsExecutor {
  def start(opts: Ambient7Options): Future[Unit] = {
    // val influxDbClient = influxdb.buildClient(opts)

    Future.failed(new Error("todo"))
  }

  // private def initDataBuffers(opts: Ambient7Options) = {}
}
