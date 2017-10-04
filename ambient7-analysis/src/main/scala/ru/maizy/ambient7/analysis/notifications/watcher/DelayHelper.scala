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
 *   trigger on from the begining, only Off() produced with duration=None
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

object DelayHelper {

  sealed trait Matched[T, V] {
    def first: (Int, T, V)
    def triggered: (Int, T, V)
  }

  case class On[T, V](firstMatch: (Int, T, V), triggered: (Int, T, V))
    extends Matched[T, V]
  {
    val first: (Int, T, V) = firstMatch

    override def toString: String = s"On(firstMatch=$firstMatch, triggered=$triggered)"
  }

  case class Off[T, V](lastMatch: (Int, T, V), triggered: (Int, T, V), duration: Option[Int])
    extends Matched[T, V]
  {
    val first: (Int, T, V) = lastMatch

    override def toString: String =
      s"Off(lastMatch=$lastMatch, triggered=$triggered, duration=${duration.getOrElse("-")})"
  }

}

class DelayHelper[T, V](
    f: T => V,
    threshold: V => Boolean,
    val delay: Int,
    val resolveDelay: Int
) {

  // use mutable accumulator because of GC penalties
  private case class Accumulators(
      var step: Int = 0,
      var prevMatched: Option[Boolean] = None,
      var first: Boolean = false,
      var onMatchedAt: Option[(Int, T, V)] = None,
      var offMatchedAt: Option[(Int, T, V)] = None,
      var stepsFromLastSwitch: Int = 0,
      var onDuration: Option[Int] = None
  )

  import DelayHelper._

  /**
   * @param data should be iterator includes points with equal delay between
   */
  def processData(data: Iterable[T]): Stream[Matched[T, V]] = {
    val iterator = data.iterator

    def loop(acc: Accumulators): Stream[Matched[T, V]] = {
      if (!iterator.hasNext) {
        Stream.empty
      } else {
        val dataItem = iterator.next
        val value = f(dataItem)
        val isMatched = threshold(value)
        val current = (acc.step, dataItem, value)

        @inline def cont(): Stream[Matched[T, V]] = {
          accUp(acc, isMatched)
          loop(acc)
        }

        @inline def emitAndContinue(event: Matched[T, V]): Stream[Matched[T, V]] = {
          accUp(acc, isMatched)
          event #:: loop(acc)
        }

        if (isMatched) {
          if (acc.prevMatched.contains(false)) {
            acc.onMatchedAt = Some(current)
            acc.stepsFromLastSwitch = 0
          }
          acc.onDuration = acc.onDuration.map(_ + 1) orElse Some(1)
          if (!acc.first && acc.stepsFromLastSwitch == delay) {
            val firstMatch = acc.onMatchedAt.getOrElse(current)
            acc.onMatchedAt = None
            emitAndContinue(On(firstMatch, current))
          } else {
            cont()
          }
        } else {
          if (acc.prevMatched.contains(true)) {
            acc.offMatchedAt = Some(current)
            acc.stepsFromLastSwitch = 0
          }
          if (!acc.first && acc.stepsFromLastSwitch + 1 == resolveDelay) {
            val lastMatch = acc.offMatchedAt.getOrElse(current)
            acc.offMatchedAt = None
            emitAndContinue(Off(lastMatch, current, acc.onDuration))
          } else {
            cont()
          }
        }

      }
    }

    def accUp(acc: Accumulators, matched: Boolean): Unit = {
      acc.step += 1
      // TODO: "Boolean expression can be simplified" why? scalastyle bug or my bug?
      // scalastyle: off
      val switchedOn = acc.prevMatched.contains(false) && matched
      val switchedOff = acc.prevMatched.contains(true) && !matched
      // scalastyle: on
      val switched = switchedOff || switchedOn

      if (acc.first && switched) {
        acc.first = false
      }
      acc.stepsFromLastSwitch += 1
      acc.prevMatched = Some(matched)

      ()
    }

    loop(Accumulators(first = true))
  }
}
