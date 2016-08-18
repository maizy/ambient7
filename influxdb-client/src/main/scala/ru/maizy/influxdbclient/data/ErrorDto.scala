package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case class ErrorDto(message: String, cause: Option[Throwable] = None)
