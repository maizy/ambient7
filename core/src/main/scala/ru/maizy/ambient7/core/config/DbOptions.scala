package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class DbOptions(
    url: Option[String] = None,
    user: String = Defaults.DB_USER,
    password: String = Defaults.DB_PASSWORD
)
