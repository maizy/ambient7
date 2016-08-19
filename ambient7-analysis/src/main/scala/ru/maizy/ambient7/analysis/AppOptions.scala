package ru.maizy.ambient7.analysis

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.data.AgentTags

case class AppOptions(
    command: Option[String] = None,
    startOfDay: Option[Int] = None,

    dbUrl: String = AppOptions.DEFAULT_DB_URL,
    dbUser: String = "ambient7",
    dbPassword: String = "",

    influxDbAgentName: String = AppOptions.DEFAULT_AGENT_NAME,
    influxDbTags: AgentTags = AgentTags.empty,
    influxDbDatabase: Option[String] = None,

    influxDbBaseUrl: String = AppOptions.DEFAULT_INFLUXDB_BASEURL,
    influxDbUser: Option[String] = None,
    influxDbPassword: Option[String] = None,

    influxDbReadonlyBaseUrl: Option[String] = None,
    influxDbReadonlyUser: Option[String] = None,
    influxDbReadonlyPassword: Option[String] = None
)

object AppOptions {
  val DEFAULT_DB_URL = "jdbc:h2:file:./target/analysis;AUTO_SERVER=TRUE"
  val DEFAULT_INFLUXDB_BASEURL = "http://localhost:8086/"
  val APP_VERSION = "0.1.0"
  val DEFAULT_AGENT_NAME = "main"
}

object OptionParser {

  private val parser = new scopt.OptionParser[AppOptions]("java -jar ambient7-analysis.jar") {

    head("ambient7: analyzing tool", AppOptions.APP_VERSION)
    help("help")
    version("version")

    opt[String]("db-url")
      .abbr("d")
      .valueName(s"<${AppOptions.DEFAULT_DB_URL}>")
      .action { (value, opts) => opts.copy(dbUrl = value) }
      .text(s"URL for connecting to h2 database")
      .required()

    opt[String]("db-user")
      .action { (value, opts) => opts.copy(dbUser = value) }
      .text("database user")

    opt[String]("db-password")
      .action { (value, opts) => opts.copy(dbUser = value) }
      .text("database password")

    cmd("aggregate-co2")
      .action { (_, opts) => opts.copy(command = Some("aggregate-co2")) }
      .text("\tcompute hourly co2 levels report")
      .children(
        opt[String]("influxdb-agent-name")
          .valueName { s"<${AppOptions.DEFAULT_AGENT_NAME}>" }
          .action { (value, opts) => opts.copy(influxDbAgentName = value) }
          .required(),

        opt[String]("influxdb-baseurl")
          .valueName { s"<${AppOptions.DEFAULT_INFLUXDB_BASEURL}>" }
          .action { (value, opts) => opts.copy(influxDbBaseUrl = value) },

        opt[String]("influxdb-database")
          .action { (value, opts) => opts.copy(influxDbDatabase = Some(value)) }
          .required(),

        opt[String]("influxdb-user")
          .action { (value, opts) => opts.copy(influxDbUser = Some(value)) },

        opt[String]("influxdb-password")
          .action { (value, opts) => opts.copy(influxDbPassword = Some(value)) },

        opt[String]("influxdb-readonly-baseurl")
          .action { (value, opts) => opts.copy(influxDbReadonlyBaseUrl = Some(value)) }
          .text("By default --influxdb-baseurl"),

        opt[String]("influxdb-readonly-user")
          .action { (value, opts) => opts.copy(influxDbReadonlyUser = Some(value)) },

        opt[String]("influxdb-readonly-password")
          .action { (value, opts) => opts.copy(influxDbReadonlyPassword = Some(value)) },

        opt[String]("influxdb-tags")
          .validate{ value => AgentTags.tryParseFromString(value).right.map(v => Unit) }
          .valueName { "<position=outdoor,altitude=200,some=val\\,ue>" }
          .action { (value, opts) => opts.copy(influxDbTags = AgentTags.apply(value)) }
          .text { "Any additional InfluxDB record tags for filtering data"}
      )

    cmd("init-db")
      .action { (_, opts) => opts.copy(command = Some("init-db")) }
      .text("\tinitialize or upgrade db")
  }

  def parse(args: Seq[String]): Option[AppOptions] = {
    var opts = parser.parse(args, AppOptions())
    val fails = Seq[Option[AppOptions] => Boolean](
      _.isEmpty,
      _.get.command.isEmpty
    )

    if (fails.exists(_(opts))) {
      parser.showUsageAsError
      None
    } else {
      opts =
        (opts.get.influxDbReadonlyUser, opts.get.influxDbReadonlyPassword) match {
          case (None, None) => opts.map{ o =>
            o.copy(
              influxDbReadonlyUser = o.influxDbUser,
              influxDbReadonlyPassword = o.influxDbPassword
            )
          }
          case _ => opts
        }

      opts = opts.get.influxDbReadonlyBaseUrl match {
        case None => opts.map { o =>
          o.copy(influxDbReadonlyBaseUrl = Some(o.influxDbBaseUrl))
        }
        case _ => opts
      }

      opts
    }
  }
}

