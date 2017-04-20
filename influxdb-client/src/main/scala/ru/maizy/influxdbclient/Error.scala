package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

class Error(message: String, cause: Option[Throwable] = None) extends Exception(message, cause.orNull)
