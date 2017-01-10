package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import spray.json.{ DefaultJsonProtocol, NullOptions }

trait BaseProtocol extends DefaultJsonProtocol with NullOptions
