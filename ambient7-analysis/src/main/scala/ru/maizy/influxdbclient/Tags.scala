package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.influxdbclient.util.Escape.{ encodeTag, decodeTag, encodeKey, encodeValue }

class TagParsingError(message: String) extends ClientException(message)

case class Tag(name: String, value: String) {
  def encoded: String = encodeTag(name) + "=" + encodeTag(value)
  def asQueryCondition: String = encodeKey(name) + " = " + encodeValue(value)
}

class Tags private(tags: IndexedSeq[Tag]) {
  def encoded: String = tags.map(_.encoded).mkString(",")
  def asQueryCondition: String =
    if (tags.isEmpty) "true" else tags.map(_.asQueryCondition).mkString(" and ")
  override def toString: String = s"Tags(${tags.mkString(",")})"
}

object Tags {

  def apply(): Tags = new Tags(IndexedSeq.empty)

  def apply(tags: Seq[Tag]): Tags =
    new Tags(
      tags
        .toIndexedSeq
        .sortWith { case (p1, p2) => p1.name.compareTo(p2.name) < 0 }
    )

  def apply(commaSeparated: String): Tags =
    tryParseFromString(commaSeparated) match {
      case Left(message) => throw new TagParsingError(message)
      case Right(tags) => tags
    }

  def tryParseFromString(commaSeparated: String): Either[String, Tags] = {
    val parts = commaSeparated.split("""(?<!\\),""").toSeq
    val parsedTags = for (
      part <- parts
      if part.count(_ == '=') == 1;
      nameValue = part.split("=")
    ) yield Tag(nameValue(0), decodeTag(nameValue(1)))

    if (parts.size != parsedTags.size) {
      // TODO: more precise errors
      Left("Some tags not parsed")
    } else {
      Right(Tags(parsedTags))
    }
  }
}
