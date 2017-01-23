package ru.maizy.ambient7.core.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.{ Ambient7Options, DbOptions, Defaults, ParsingError }


trait MainDbConfigReader extends UniversalConfigReader {

  import UniversalConfigReader._

  private def mainDbOpts(opts: Ambient7Options)(fill: DbOptions => DbOptions): Ambient7Options = {
    opts.copy(mainDb = Some(fill(opts.mainDb.getOrElse(DbOptions()))))
  }

  private def appendDbOptsCheck(check: DbOptions => CheckResult): Unit =
    appendCheck { appOpts =>
      appOpts.mainDb match {
        case Some(dbOpts) => check(dbOpts)
        case _ => Left(ParsingError.withMessage("DB opts not defined"))
      }
    }

  def fillDbOptions(): Unit = {

    // TODO: uni config rules

    cliParser.opt[String]("db-url")
      .abbr("d")
      .valueName(s"<${Defaults.DB_URL}>")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(url = Some(value))) }
      .text(s"URL for connecting to h2 database")

    appendSimpleOptionalConfigRule[String]("db.url") { (value, opts) =>
      mainDbOpts(opts)(_.copy(url = Some(value)))
    }

    appendDbOptsCheck{ dbOpts => Either.cond(dbOpts.url.isDefined, (), ParsingError.withMessage("db-url is required")) }


    cliParser.opt[String]("db-user")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(user = value)) }
      .text("database user")

    appendSimpleOptionalConfigRule[String]("db.user") {
      (value, opts) => mainDbOpts(opts)(_.copy(user = value))
    }


    cliParser.opt[String]("db-password")
      .action { (value, opts) => mainDbOpts(opts)(_.copy(password = value)) }
      .text("database password")

    appendSimpleOptionalConfigRule[String]("db.password") { (value, opts) =>
      mainDbOpts(opts)(_.copy(password = value))
    }

    ()
  }
}
