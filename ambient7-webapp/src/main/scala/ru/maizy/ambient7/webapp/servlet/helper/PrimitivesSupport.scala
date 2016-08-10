package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.util.{ Failure, Success, Try }
import org.scalatra.ScalatraBase


trait PrimitivesSupport extends ScalatraBase {

  @throws(classOf[NoSuchElementException])
  @throws(classOf[IllegalArgumentException])
  def intParam(key: String): Int = {
    val raw = params(key)

    Try(raw.toInt) match {
      case Success(int) => int
      case Failure(e) => throw new IllegalThreadStateException("Unable to parse int \"" + raw + "\": " + e.getMessage)
    }
  }

  def optIntParam(key: String): Option[Int] =
    Try(intParam(key)).toOption

}
