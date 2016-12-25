package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.{ MainDbConfigParser, UniversalConfigParser }

object WebAppConfigParser
  extends UniversalConfigParser
  with MainDbConfigParser
{
  override def appName: String = "java -jar ambient7-webapp.jar"

  override def fillParser(): Unit = {
    super.fillParser()
    fillDbOptions()
  }
}
