package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

class ClientException(message: String, cause: Option[Throwable] = None)
  extends Exception(message, cause.orNull)
