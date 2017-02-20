package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

class AgentTagParsingError(message: String) extends RuntimeException(message)

case class AgentTag(name: String, value: String) {
  import ru.maizy.influxdbclient.util.Escape.encodeTag
  def encoded: String = encodeTag(name) + "=" + encodeTag(value)
}

class AgentTags private(val tags: IndexedSeq[AgentTag]) {
  def encoded: String = tags.map(_.encoded).mkString(",")
  override def toString: String = s"AgentTags(${tags.mkString(",")})"
  def asPairs: IndexedSeq[(String, String)] = tags.map(t => (t.name, t.value))

  def canEqual(other: Any): Boolean = other.isInstanceOf[AgentTags]

  override def equals(other: Any): Boolean = other match {
    case that: AgentTags => (that canEqual this) && tags == that.tags
    case _ => false
  }

  override def hashCode(): Int = {
    tags.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object AgentTags {

  import ru.maizy.influxdbclient.util.Escape.decodeTag

  def apply(): AgentTags = empty

  def empty: AgentTags = new AgentTags(IndexedSeq.empty)

  def apply(tags: Seq[AgentTag]): AgentTags =
    new AgentTags(
      tags
        .toIndexedSeq
        .sortWith { case (p1, p2) => p1.name.compareTo(p2.name) < 0 }
    )

  def apply(commaSeparated: String): AgentTags =
    tryParseFromString(commaSeparated) match {
      case Left(message) => throw new AgentTagParsingError(message)
      case Right(tags) => tags
    }

  def tryParseFromString(commaSeparated: String): Either[String, AgentTags] = {
    if (commaSeparated == "") {
      Right(AgentTags.empty)
    } else {
      val parts = commaSeparated.split("""(?<!\\),""").toSeq
      val parsedTags = for (
        part <- parts
        if part.count(_ == '=') == 1;
        nameValue = part.split("=")
      ) yield AgentTag(decodeTag(nameValue(0)), decodeTag(nameValue(1)))

      if (parts.size != parsedTags.size) {
        // TODO: more precise errors
        Left("Some tags not parsed")
      } else {
        Right(AgentTags(parsedTags))
      }
    }
  }
}
