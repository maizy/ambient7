package ru.maizy.ambient7.webapp.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.json.{ BaseProtocol, Co2AgentIdProtocol }
import ru.maizy.ambient7.webapp.data.Co2Device

trait Co2DeviceProtocol extends BaseProtocol with Co2AgentIdProtocol {
  implicit val deviceFormat = jsonFormat(Co2Device, "id", "agent")
}

object Co2DeviceProtocol extends Co2DeviceProtocol
