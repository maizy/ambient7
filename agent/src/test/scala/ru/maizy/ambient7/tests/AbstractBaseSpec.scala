package ru.maizy.ambient7.tests


/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import org.scalatest.{ FlatSpec, Matchers }
import ru.maizy.ambient7.{ Event, Writer }

abstract class AbstractBaseSpec extends FlatSpec with Matchers

trait WritersTestUtils {

  def checkWriterEvent(writer: Writer, event: Event): (String, String) = {
    checkWriterOutput(writer)(_.write(event))
  }

  def checkWriterInit(writer: Writer): (String, String) = {
    checkWriterOutput(writer)(_.onInit())
  }

  private def checkWriterOutput(writer: Writer)(f: Writer => Unit): (String, String) = {
    val out = new java.io.ByteArrayOutputStream
    val err = new java.io.ByteArrayOutputStream
    Console.withOut(out) {
      Console.withErr(err) {
        f(writer)
      }
    }
    (out.toString, err.toString)
  }
}
