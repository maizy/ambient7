package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
@deprecated("use Throwable", "0.4")
case class ErrorDto(message: String, cause: Option[Throwable] = None)
