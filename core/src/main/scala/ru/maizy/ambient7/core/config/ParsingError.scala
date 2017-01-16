package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

case class ParsingError(messages: IndexedSeq[String] = IndexedSeq.empty, usage: Option[String] = None) {
  def merge(other: ParsingError): ParsingError = {
    val mergedUsage = (usage, other.usage) match {
      case (Some(text), Some(otherText)) => Some(text + "\n" + otherText)
      case (None, otherUsage@Some(_)) => otherUsage
      case (thisUsage@Some(_), None) => thisUsage
      case _ => None
    }
    copy(messages ++ other.messages, mergedUsage)
  }

  def appendUsage(additionalUsage: String): ParsingError =
    copy(usage = Some(Seq(usage.getOrElse(""), additionalUsage).mkString("\n")))
}

object ParsingError {
  def merge(a: ParsingError, b: ParsingError): ParsingError = a merge b

  def withMessage(message: String): ParsingError =
    ParsingError(IndexedSeq(message))

  def withMessages(messages: Seq[String]): ParsingError =
    ParsingError(messages.toIndexedSeq)

}
