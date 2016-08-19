package ru.maizy.ambient7.webapp

import ru.maizy.ambient7.core.data.MT8057AgentId

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class MT8057Device(id: String, agentId: MT8057AgentId)

case class AppConfig(
    dbUrl: String = "jdbc:h2:file:./analysis;AUTO_SERVER=TRUE",
    dbUser: String = "ambient7",
    dbPassword: String = "",
    mt8057Devices: Map[String, MT8057Device] = Map.empty
)
