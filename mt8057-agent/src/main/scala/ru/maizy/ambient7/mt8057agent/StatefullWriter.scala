package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
abstract class StatefullWriter extends Writer {
  protected var connected = false
  protected var lastStateUpdate: Option[Long] = None
  protected var co2: Option[Co2] = None
  protected var temp: Option[Temp] = None

  def detectState(event: Event): Unit = {
    event match {
      case DeviceUp(ts) if !connected =>
        lastStateUpdate = Some(ts)
        connected = true
      case DeviceDown(ts) if connected =>
        lastStateUpdate = Some(ts)
        connected = false
      case Co2Updated(v, _) => co2 = Some(v)
      case TempUpdated(v, _) => temp = Some(v)
      case _ =>
    }
  }

  override def write(event: Event): Unit = {
    detectState(event)
    internalWrite(event)
  }

  protected def internalWrite(event: Event): Unit
}
