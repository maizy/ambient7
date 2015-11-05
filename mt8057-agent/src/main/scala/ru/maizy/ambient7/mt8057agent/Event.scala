package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

// all times in nanoseconds

sealed trait Event

case class Co2Updated(co2: Co2, timestamp: Long) extends Event
case class TempUpdated(temp: Temp, timestamp: Long) extends Event
case class DeviceUp(timestamp: Long) extends Event
case class DeviceDown(timestamp: Long) extends Event
