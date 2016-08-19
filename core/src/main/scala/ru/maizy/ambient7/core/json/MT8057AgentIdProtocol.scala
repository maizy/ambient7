package ru.maizy.ambient7.core.json

import ru.maizy.ambient7.core.data.MT8057AgentId

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

trait MT8057AgentIdProtocol extends BaseProtocol with AgentTagsProtocol {

  implicit val agentIdProtocol = jsonFormat(MT8057AgentId, "name", "tags")
}
