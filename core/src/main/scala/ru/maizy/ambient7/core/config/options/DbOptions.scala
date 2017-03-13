package ru.maizy.ambient7.core.config.options

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Defaults

case class DbOptions(
    url: Option[String] = None,
    user: String = Defaults.DB_USER,
    password: String = Defaults.DB_PASSWORD
)
