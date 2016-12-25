package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
trait MainDbConfigParser extends UniversalConfigParser {

  private def mainDbOpts(opts: Ambient7Options)(fill: DbOptions => DbOptions): Ambient7Options = {
    opts.copy(mainDb = Some(fill(opts.mainDb.getOrElse(DbOptions()))))
  }

  private def appendDbOptsCheck(check: DbOptions => Either[String, Unit]): Unit = {
    appendCheck {
      case Some(appOpts) => appOpts.mainDb match {
        case Some(dbOpts) => check(dbOpts)
        case _ => Left("DB opts not defined")
        }
      case _ => Left("App opts not defined")
    }
  }

  def fillDbOptions(): Unit = {

    parser.opt[String]("db-url")
      .abbr("d")
      .valueName(s"<${Defaults.DB_URL}>")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(url = Some(value))) }
      .text(s"URL for connecting to h2 database")

    parser.opt[String]("db-user")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(user = value)) }
      .text("database user")

    parser.opt[String]("db-password")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(password = value)) }
      .text("database password")

    // TODO: uni config rules

    appendDbOptsCheck{ dbOpts => Either.cond(dbOpts.url.isDefined, Unit, "db-url is required") }
  }
}
