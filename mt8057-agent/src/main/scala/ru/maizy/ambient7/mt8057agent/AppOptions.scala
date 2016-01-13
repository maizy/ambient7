package ru.maizy.ambient7.mt8057agent

import java.io.File

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
trait EnumerationMap extends Enumeration {
  self =>
  lazy val valuesMap = self.values.map{ v => (v.toString, v) }.toMap
}

object Writers extends Enumeration with EnumerationMap {
  self =>
  type Writer = Value
  val Console = Value("console")
  val Interactive = Value("interactive")
  val InfluxDb = Value("influxdb")
}

case class AppOptions(
    agentName: String = AppOptions.DEFAULT_AGENT_NAME,
    writers: Set[Writers.Writer] = Set.empty,
    influxDbBaseUrl: String = AppOptions.DEFAULT_INFLUXDB_BASEURL,
    influxDbDatabase: Option[String] = None,
    influxDbUser: Option[String] = None,
    influxDbPassword: Option[String] = None,
    influxDbAgentName: Option[String] = None,
    influxDbTags: String = "",
    useEmulator: Boolean = false,
    logFile: Option[File] = None,
    verboseLogging: Boolean = false
)

object AppOptions {
  val DEFAULT_INFLUXDB_BASEURL = "http://localhost:8086/write"
  val DEFAULT_AGENT_NAME = "main"
  val APP_VERSION = "0.1.0"  // TODO: auto generate on build
}

object OptionParser {

  private val parser = new scopt.OptionParser[AppOptions]("java -jar ambient7-mt8057-agent.jar") {

    private def allInEnum(enum: EnumerationMap, values: Set[String]): Either[String, Unit] = {
      if (values.exists(!enum.valuesMap.contains(_))) {
        val allowed = enum.valuesMap.keys
        failure(s"Some of '${ values.mkString(",") }' not in allowed values list (${ allowed.mkString(", ") })")
      } else {
        success
      }
    }

    private def enumValues(enum: EnumerationMap) = enum.valuesMap.keys.mkString("|")

    head("ambient7 agent for MT8057 CO2 detector", AppOptions.APP_VERSION)
    help("help")
    version("version")

    opt[Seq[String]]("writers")
      .valueName { enumValues(Writers) }
      .validate { v => allInEnum(Writers,  v.toSet) }
      .action { (value, opts) => opts.copy(writers = value.toSet.map(Writers.valuesMap.apply)) }
      .text { "One or more writers divided by comma. But only one of console or interactive" }
      .required()

    opt[String]("agent-name")
      .valueName { AppOptions.DEFAULT_AGENT_NAME }
      .action { (value, opts) => opts.copy(influxDbBaseUrl = value) }

    opt[String]("influxdb-baseurl")
      .valueName { AppOptions.DEFAULT_INFLUXDB_BASEURL }
      .action { (value, opts) => opts.copy(influxDbBaseUrl = value) }

    opt[String]("influxdb-database")
      .action { (value, opts) => opts.copy(influxDbDatabase = Some(value)) }

    opt[String]("influxdb-user")
      .action { (value, opts) => opts.copy(influxDbUser = Some(value)) }

    opt[String]("influxdb-password")
      .action { (value, opts) => opts.copy(influxDbPassword = Some(value)) }

    opt[String]("influxdb-tags")
      .valueName { "position=outdoor,altitude=200,some=val\\,ue" }
      .action { (value, opts) => opts.copy(influxDbTags = value) }
      .text { "Any additional InfluxDB record tags"}

    opt[Unit]("emulator")
      .text {
        "Emulator mode. Generate random data instead of device support." +
        "Do not use with the real DBs, because you will write a lots of crap to them."}
      .action { (_, opts) => opts.copy(useEmulator = true) }

    opt[File]("log-file")
      .action { (file, opts) => opts.copy(logFile = Some(file)) }

    opt[Unit]("verbose")
      .action { (_, opts) => opts.copy(verboseLogging = true) }

  }

  def parse(args: Seq[String]): Option[AppOptions] = {
    var opts = parser.parse(args, AppOptions())
    if (opts.isDefined && opts.get.influxDbAgentName.isEmpty) {
      opts = Some(opts.get.copy(influxDbAgentName = Some(opts.get.agentName)))
    }
    val fails = Seq[Option[AppOptions] => Boolean](
      _.isEmpty,
      _.get.writers.isEmpty,
      o => o.get.writers.contains(Writers.Interactive) && o.get.writers.contains(Writers.Console),
      { o =>
        o.get.writers.contains(Writers.InfluxDb) &&
          Seq(o.get.influxDbDatabase, o.get.influxDbAgentName).exists(_.isEmpty) }

    )
    if (fails.exists(_(opts))) {
      parser.showUsageAsError
      None
    } else {
      opts
    }
  }
}
