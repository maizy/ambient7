package ru.maizy.ambient7.core.config.options

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.io.File

// TODO: should be only in mt8057-agent submodule

trait EnumerationMap extends Enumeration {
  self =>
  lazy val valuesMap: Map[String, Value] = self.values.map{ v => (v.toString, v) }.toMap
}

object Writers extends Enumeration with EnumerationMap {
  self =>
  type Writer = Value
  val Console = Value("console")
  val Interactive = Value("interactive")
  val InfluxDb = Value("influxdb")
}

case class Mt8057AgentSpecificOptions(
    writers: Set[Writers.Writer] = Set.empty,
    useEmulator: Boolean = false,
    logFile: Option[File] = None,
    verboseLogging: Boolean = false
)
