package ru.maizy.ambient7.analysis.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.influxdbclient.Tags

case class AgentId(agentName: String, tags: Tags)
