package ru.maizy.influxdbclient.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
object Escape {

  def encodeKey(key: String): String = key.replace("\"", "\\\"")  // " -> \"

  def escapeKey(key: String): String = "\"" + encodeKey(key) + "\""


  def encodeTag(key: String): String = key.replace(",", "\\,")

  def decodeTag(key: String): String = key.replace("\\,", ",")


  // TODO: build right replacement based on spec
  def encodeValue(value: String): String = value.replace("'", "\\'")

  def escapeValue(value: String): String = "'" + encodeValue(value) + "'"

  def tagsToQueryCondition(tags: IndexedSeq[(String, String)]): String = {
    if (tags.isEmpty) {
      "true"
    } else {
      tags
        .map { pair =>
          val (name, value) = pair
          encodeKey(name) + " = " + encodeValue(value)
        }
        .mkString(" and ")
    }
  }
}
