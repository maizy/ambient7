package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.{ MainDbConfigReader, UniversalConfigReader }

object WebAppConfigReader
  extends UniversalConfigReader
  with MainDbConfigReader
{
  override def appName: String = "java -jar ambient7-webapp.jar"

  override def fillReader(): Unit = {
    fillConfigOptions(requireConfig = true)
    fillDbOptions()
  }
}
