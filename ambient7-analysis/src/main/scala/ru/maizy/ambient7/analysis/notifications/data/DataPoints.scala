package ru.maizy.ambient7.analysis.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import scala.collection.mutable

class DataPoints[T](val limit: Int)
{
  require(limit > 0)

  private val underlying = new mutable.ListBuffer[(ZonedDateTime, T)]

  def appendPoint(time: ZonedDateTime, value: T): Unit = {
    // TODO: more precise locking
    synchronized {
      val replaceElementIndex = underlying.indexWhere(pair => pair._1.compareTo(time) == 0)
      if (replaceElementIndex != -1) {
        underlying(replaceElementIndex) = (time, value)
      } else {
        val nextAfter = underlying.reverse.indexWhere(pair => pair._1.compareTo(time) < 0)
        val appendPosition = if (nextAfter == -1) 0 else underlying.length - nextAfter
          underlying.insert(appendPosition, (time, value))
      }
      if (underlying.length > limit) {
        underlying.remove(0, underlying.length - limit)
      }
    }
    ()
  }

  def toTraversable: Traversable[(ZonedDateTime, T)] = underlying

  override def toString: String = {
    val points = toTraversable.map { case (time, v) => s"$time => $v"}.mkString("\n\t")
    s"DataPoints(\n\t$points\n)"
  }


}
