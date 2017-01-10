package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import scala.math.BigDecimal

object ValueType extends Enumeration {
  val Null, String, Number = Value
}

sealed trait Value {
  type T <: Any
  def valueType: ValueType.Value
  def value: T

  override def toString: String = s"Value(${value.toString})"
}

// TODO: is there any better way?
object NullValue extends Value {
  type T = Option[Any]
  val valueType = ValueType.Null
  val value: Option[Any] = None
}

abstract class SimpleValue[C](v: C, val valueType: ValueType.Value) extends Value {
  type T = C
  val value: C = v


  def canEqual(other: Any): Boolean =
    other.isInstanceOf[SimpleValue[C]]

  override def equals(other: Any): Boolean = other match {
    case that: SimpleValue[C] =>
      (that canEqual this) &&
        value == that.value
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(value)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

class StringValue(v: String) extends SimpleValue[String](v, ValueType.String)

object StringValue {
  def apply(str: String): StringValue = new StringValue(str)
}

class NumberValue(v: BigDecimal) extends SimpleValue[BigDecimal](v, ValueType.Number)

object NumberValue {
  def apply(v: BigDecimal): NumberValue = new NumberValue(v)
  def apply(v: Int): NumberValue = apply(BigDecimal(v))
  def apply(v: Double): NumberValue = apply((BigDecimal(v)))
}
