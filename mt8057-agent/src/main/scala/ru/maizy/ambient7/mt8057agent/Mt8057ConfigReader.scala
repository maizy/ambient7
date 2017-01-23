package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.reader.{ DevicesConfigReader, InfluxDbConfigReader, UniversalConfigReader }
import ru.maizy.ambient7.core.data.DeviceType

object Mt8057ConfigReader
  extends UniversalConfigReader
  with DevicesConfigReader
  with InfluxDbConfigReader
{
  override def appName: String = "java -jar ambient7-mt8057-agent.jar"

  override def fillReader(): Unit = {
    fillConfigOptions()
    fillInfluxDbOptions()
    fillDevicesOptions()
    fillDeviceFromCliOptions(DeviceType.CO2)
    fillSelectedDeviceOptions()
    co2DeviceRequired()
  }

  fillReader()
}


