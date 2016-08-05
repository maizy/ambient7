package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.influxdbclient.data.{ NumberValue, StringValue }

class SimpleValueSpec extends BaseSpec {

  "SimpleValueSpec" should "implements equals & hashCode for all subtypes" in {
    val s1 = StringValue("1")
    val s2 = StringValue("2")
    val s1eq = StringValue("1")
    val n1 = NumberValue(2)
    val n1eq = NumberValue(2)
    val n2 = NumberValue(3)

    assert(s1 == s1eq)
    assert(s1 != s2)
    assert(s1 != n1)
    assert(n1 == n1eq)
    assert(n1 != n2)
    assert(n1 != s2)
  }

}
