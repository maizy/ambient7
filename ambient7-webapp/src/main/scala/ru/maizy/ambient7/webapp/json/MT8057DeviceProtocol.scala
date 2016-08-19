package ru.maizy.ambient7.webapp.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.json.{ BaseProtocol, MT8057AgentIdProtocol }
import ru.maizy.ambient7.webapp.data.MT8057Device

object MT8057DeviceProtocol extends BaseProtocol with MT8057AgentIdProtocol {
  implicit val device = jsonFormat(MT8057Device, "id", "agent")
}
