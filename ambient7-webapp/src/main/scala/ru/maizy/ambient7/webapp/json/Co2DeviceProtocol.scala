package ru.maizy.ambient7.webapp.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.data.Co2Device
import ru.maizy.ambient7.core.json.{ BaseProtocol, Co2AgentProtocol }

trait Co2DeviceProtocol extends BaseProtocol with Co2AgentProtocol {
  implicit val deviceFormat = jsonFormat(Co2Device, "id", "agent")
}

object Co2DeviceProtocol extends Co2DeviceProtocol
