package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.nio.file.Path

case class Ambient7Options(
    universalConfigPath: Option[Path] = None,
    influxDb: Option[InfluxDbOptions] = None,
    mainDb: Option[DbOptions] = None
)
