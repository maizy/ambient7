package ru.maizy.ambient7.core.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime

case class Co2AggregatedLevels(
    lowLevel: Int,
    mediumLevel: Int,
    highLevel: Int,
    unknownLevel: Int,
    from: ZonedDateTime,
    to: ZonedDateTime,
    agentId: Co2Agent
) {
  override def toString: String = s"Co2AggregatedLevels(low=$lowLevel, med=$mediumLevel, high=$highLevel, " +
      s"unknown=$unknownLevel, $from->$to, agent=${agentId.agentName}, tags=${agentId.tags})"

  def hasAnyResult: Boolean =
    lowLevel > 0 || mediumLevel > 0 || highLevel > 0

  def combine(other: Co2AggregatedLevels): Co2AggregatedLevels =
    copy(
      lowLevel = lowLevel + other.lowLevel,
      mediumLevel = mediumLevel + other.mediumLevel,
      highLevel = highLevel + other.highLevel,
      unknownLevel = unknownLevel + other.unknownLevel,
      if (other.from.compareTo(from) < 0) other.from else from,
      if (other.to.compareTo(to) > 0) other.to else to
    )
}
