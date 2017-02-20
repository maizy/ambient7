package ru.maizy.ambient7.core.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import spray.json.{ DefaultJsonProtocol, NullOptions }

trait BaseProtocol extends DefaultJsonProtocol with NullOptions
