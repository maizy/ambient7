package ru.maizy.influxdbclient.responses

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.influxdbclient.dto.ErrorDto


trait ErrorProtocol extends BaseProtocol {
  implicit val errorFormat = jsonFormat(ErrorDto, "error")
}

object ErrorProtocol extends ErrorProtocol

// ex:
//  {
//     "error": "error parsing query: found ..."
//  }

