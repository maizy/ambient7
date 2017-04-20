package ru.maizy.ambient7.analysis.notifications.watcher

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

/*
 * How it works?
 *
 * dataItem <- data
 *
 * output of f(dataItem):
 *
 *  f() == true        |----------*-------------------|                              |-----|   |
 *                     |                              |                              |     |   |
 *                     | <-------- duration  -------> |                              |     |   |
 *  f() == false  _____|                              |___________*__________________|     |___|___
 *  _
 *
 *
 *                    ^          ^                   ^           ^                   ^        ^
 *                    |          |                   |           |                   |        |
 *                  first   On() produced      last match     Off() produced        nothing produced
 *                  match   after delay       (Off.begin)     after resolveDelay    for those fluctuations
 *               (On.begin)
 *
 *
 * Special cases
 *
 *   trigger on from the begining, only Off() produced with duration=None, begin = None
 *
 *   --------------|
 *                 |______*_____
 *
 *
 *   trigger on until the end, only On() produced
 *
 *            |------*-----------
 *    ________|
 *
 *
 * */

object ThresholdHelper {
  sealed trait Matched[T, V] {
    def begin: Option[(Int, V, T)]
    def triggered: (Int, V, T)
  }
  case class Off[T, V](begin: Option[(Int, V, T)], triggered: (Int, V, T), duration: Option[Int]) extends Matched[T, V]
  case class On[T, V](begin: Option[(Int, V, T)], triggered: (Int, V, T)) extends Matched[T, V]
}

class ThresholdHelper[T, V](
    f: T => V,
    threshold: V => Boolean,
    val delay: Int,
    val resolveDelay: Int
) {
  import ThresholdHelper._

  def processData(data: Iterable[T]): Stream[Matched[T, V]] = ???
  /* {
    val iterator = data.iterator

    def loop(
        step: Int,
        matchedSteps: Int = 0,
        firstMatch: Option[(Int, V, T)] = None,
        first: Boolean = true
      ): Stream[Matched[T, V]] =
    {
      if (!iterator.hasNext) {
        Stream.empty
      } else {
        val dataItem = iterator.next
        val value = f(dataItem)
        val isMatched = threshold(value)
        if (isMatched) {
          val current = (step, value, dataItem)
          val begin = firstMatch.getOrElse(current)
          // FIXME first match condition
          On(Some(begin), current) #:: loop(step + 1, matchedSteps + 1, Some(begin))
        } else {
          loop(step + 1, 0, None)
        }
      }
    }

    loop(0)
  }*/
}
