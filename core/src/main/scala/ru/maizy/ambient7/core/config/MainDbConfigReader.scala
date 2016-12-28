package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
trait MainDbConfigReader extends UniversalConfigReader {

  private def mainDbOpts(opts: Ambient7Options)(fill: DbOptions => DbOptions): Ambient7Options = {
    opts.copy(mainDb = Some(fill(opts.mainDb.getOrElse(DbOptions()))))
  }

  private def appendDbOptsCheck(check: DbOptions => CheckResult): Unit =
    appendCheck { appOpts =>
      appOpts.mainDb match {
        case Some(dbOpts) => check(dbOpts)
        case _ => Left(IndexedSeq("DB opts not defined"))
      }
    }

  def fillDbOptions(): Unit = {

    // TODO: uni config rules

    cliParser.opt[String]("db-url")
      .abbr("d")
      .valueName(s"<${Defaults.DB_URL}>")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(url = Some(value))) }
      .text(s"URL for connecting to h2 database")

    appendDbOptsCheck{ dbOpts => Either.cond(dbOpts.url.isDefined, (), IndexedSeq("db-url is required")) }


    cliParser.opt[String]("db-user")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(user = value)) }
      .text("database user")


    cliParser.opt[String]("db-password")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(password = value)) }
      .text("database password")

    ()
  }
}
