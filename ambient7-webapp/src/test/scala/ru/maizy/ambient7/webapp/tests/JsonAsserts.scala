package ru.maizy.ambient7.webapp.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
import org.json4s._
import org.json4s.native.JsonMethods._
import org.scalatest.Matchers

trait JsonAsserts extends Matchers {

  def parseJson(data: String): JValue = parse(data)

  def assertJson(real: String)(expected: String): JValue = {
    assertJson(parseJson(real))(expected)
  }

  def assertJson(real: JValue)(expected: String): JValue = {
    val parsedExpected = parseJson(expected)

    val Diff(changed, added, deleted) = real diff parsedExpected
    val hasDiff = changed != JNothing || added != JNothing || deleted != JNothing
    def p(v: JValue) = v match {
      case JNothing => "[None]"
      case _ => pretty(render(v))
    }
    if (hasDiff) {
      val elems = List(
        ("added", added), ("deleted", deleted), ("changed", changed), ("expected", parsedExpected),
        ("real", real)
      )
      fail(
        "Json not equals" +
        elems.map { case (label, json) => s"\n$label\n${p(json)}" }.mkString("\n")
      )
    }
    real
  }

}
