package ru.maizy.ambient7.core.json

import ru.maizy.ambient7.core.data.Co2AgentId

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

trait Co2AgentIdProtocol extends BaseProtocol with AgentTagsProtocol {

  implicit val agentIdProtocol = jsonFormat(Co2AgentId, "name", "tags")
}
