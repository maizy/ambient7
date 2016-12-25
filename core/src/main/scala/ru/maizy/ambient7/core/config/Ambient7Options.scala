package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class Ambient7Options(
    mainConfigPath: Option[String] = None,
    mainInfluxDd: Option[InfluxDbOptions] = None,
    mainDb: Option[DbOptions] = None
)
