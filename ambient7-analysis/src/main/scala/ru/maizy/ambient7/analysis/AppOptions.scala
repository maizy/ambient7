package ru.maizy.ambient7.analysis

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class AppOptions(
    dbUrl: String = AppOptions.DEFAULT_DB_URL,
    command: Option[String] = None,
    startOfDay: Option[Int] = None
)

object AppOptions {
  val DEFAULT_DB_URL = "jdbc:h2:file:./target/analysis;AUTO_SERVER=TRUE"
  val APP_VERSION = "0.1.0"
}

object OptionParser {

  private val parser = new scopt.OptionParser[AppOptions]("java -jar ambient7-analysis.jar") {

    head("ambient7: analyzing tool", AppOptions.APP_VERSION)
    help("help")
    version("version")

    opt[String]("db-url")
      .abbr("d")
      .action { (value, opts) => opts.copy(dbUrl = value) }
      .text(s"URL for connecting to h2 database, ex.: ${AppOptions.DEFAULT_DB_URL}")
      .required()

    cmd("aggregate-co2")
      .action { (_, opts) => opts.copy(command = Some("aggregate-co2")) }
      .text("compute hourly co2 levels report")
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
      opts
    }
  }
}

