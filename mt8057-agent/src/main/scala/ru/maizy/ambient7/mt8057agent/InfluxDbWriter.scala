package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class InfluxDbWriter(opts: AppOptions) extends Writer {
  override def write(event: Event): Unit = {
    print(s"TODO (influxdb writer): $event")
  }

  override def onInit(): Unit = {
    print(s"TODO (influxdb writer): init")
  }
}
