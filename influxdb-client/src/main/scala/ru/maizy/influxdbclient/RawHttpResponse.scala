package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

case class RawHttpResponse(code: Int, body: Array[Byte], headers: Map[String, IndexedSeq[String]])
