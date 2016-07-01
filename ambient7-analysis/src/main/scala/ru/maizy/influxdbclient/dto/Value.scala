package ru.maizy.influxdbclient.dto

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

object ValueType extends Enumeration {
  val String, Int, Double = Value
}

sealed trait Value {
  type T <: Any
  def valueType: ValueType.Value
  def value: T
}

abstract class SimpleValue[C](v: C, val valueType: ValueType.Value) {
  type T = C
  val value: C = v
}

class StringValue(v: String) extends SimpleValue[String](v, ValueType.Int)
class IntValue(v: Int) extends SimpleValue[Int](v, ValueType.Int)
class DoubleValue(v: Double) extends SimpleValue[Double](v, ValueType.Double)
