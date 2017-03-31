package ru.maizy.influxdbclient.util

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
object Escape {

  def encodeIdentifier(key: String): String = key.replace("\"", "\\\"")  // " -> \"

  def escapeIdentifier(key: String): String = "\"" + encodeIdentifier(key) + "\""

  def decodeIdentifier(value: String): String = value.replace("\\\"", "\"")


  def encodeValue(value: String): String = value.replace("'", "\\'")

  def decodeValue(value: String): String = value.replace("\\'", "'")

  def escapeValue(value: String): String = "'" + encodeValue(value) + "'"


  def encodeTag(key: String): String = key.replace(",", "\\,")

  def decodeTag(key: String): String = key.replace("\\,", ",")

  def tagsToQueryCondition(tags: IndexedSeq[(String, String)]): String = {
    if (tags.isEmpty) {
      "true"
    } else {
      tags
        .map { pair =>
          val (name, value) = pair
          encodeIdentifier(name) + " = " + encodeValue(value)
        }
        .mkString(" and ")
    }
  }
}
