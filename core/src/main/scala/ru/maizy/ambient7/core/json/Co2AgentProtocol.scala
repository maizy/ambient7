package ru.maizy.ambient7.core.json

import ru.maizy.ambient7.core.data.Co2Agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

trait Co2AgentProtocol extends BaseProtocol with AgentTagsProtocol {

  implicit val agentProtocol = jsonFormat(Co2Agent, "name", "tags")
}
