package ru.maizy.ambient7.core.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.nio.file.Paths
import org.scalatest.{ FlatSpec, Matchers }

abstract class BaseSpec extends FlatSpec with Matchers {

  def getResourcePathString(relPath: String): String = {
    val normPath = if (!relPath.startsWith("/")) "/" + relPath else relPath
    Paths.get(this.getClass.getResource(normPath).toURI).toString
  }
}
