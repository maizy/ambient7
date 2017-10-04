package ru.maizy.ambient7.analysis.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

sealed trait AggregateType {
  def code: String
  def apply(value: String): String
}

object MAX extends AggregateType {
  override def code: String = "max"
  override def apply(value: String): String = s"max($value)"
}

object MIN extends AggregateType {
  override def code: String = "min"
  override def apply(value: String): String = s"min($value)"
}

object MEAN extends AggregateType {
  override def code: String = "mean"
  override def apply(value: String): String = s"mean($value)"
}
