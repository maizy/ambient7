package ru.maizy.ambient7.core.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.{ Ambient7Options, Defaults, InfluxDbOptions, ParsingError }


trait InfluxDbConfigReader extends UniversalConfigReader {

  private def influxDbOpts(opts: Ambient7Options)(fill: InfluxDbOptions => InfluxDbOptions): Ambient7Options = {
    opts.copy(influxDb = Some(fill(opts.influxDb.getOrElse(InfluxDbOptions()))))
  }

  private def appendInfluxDbOptsCheck(check: InfluxDbOptions => Either[ParsingError, Unit]): Unit =
    appendCheck { appOpts =>
      appOpts.influxDb match {
        case Some(influxDbOptions) => check(influxDbOptions)
        case _ => Left(ParsingError.withMessage("InfluxDB opts not defined"))
      }
    }

  def fillInfluxDbOptions(): Unit = {

    cliParser.opt[String]("influxdb-baseurl")
      .valueName { s"<${Defaults.INFLUXDB_BASEURL}>" }
      .action { (value, opts) => influxDbOpts(opts)(_.copy(baseUrl = value)) }

    appendSimpleOptionalConfigRule[String]("influxdb.baseurl") { (value, opts) =>
      influxDbOpts(opts)(_.copy(baseUrl = value))
    }


    cliParser.opt[String]("influxdb-database")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(database = Some(value))) }
      .required()

    appendSimpleOptionalConfigRule[String]("influxdb.database") { (value, opts) =>
      influxDbOpts(opts)(_.copy(baseUrl = value))
    }

    appendInfluxDbOptsCheck({
      opts => Either.cond(opts.database.isDefined, (), ParsingError.withMessage("influxdb-database is required"))
    })


    cliParser.opt[String]("influxdb-user")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(user = Some(value))) }

    appendSimpleOptionalConfigRule[String]("influxdb.user") { (value, opts) =>
      influxDbOpts(opts)(_.copy(user = Some(value)))
    }

    appendInfluxDbOptsCheck({
      opts => Either.cond(opts.user.isDefined, (), ParsingError.withMessage("influxdb-user is required"))
    })


    cliParser.opt[String]("influxdb-password")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(password = Some(value))) }

    appendSimpleOptionalConfigRule[String]("influxdb.password") { (value, opts) =>
      influxDbOpts(opts)(_.copy(password = Some(value)))
    }

    appendInfluxDbOptsCheck({
      opts => Either.cond(opts.password.isDefined, (), ParsingError.withMessage("influxdb-password is required"))
    })


    cliParser.opt[String]("influxdb-readonly-baseurl")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(readonlyBaseUrl = Some(value))) }
      .text("By default --influxdb-baseurl")

    appendSimpleOptionalConfigRule[String]("influxdb.readonly.baseurl") { (value, opts) =>
      influxDbOpts(opts)(_.copy(readonlyBaseUrl = Some(value)))
    }

    appendInfluxDbOptsCheck({
      opts => Either.cond(
        opts.readonlyBaseUrl.isDefined,
        (),
        ParsingError.withMessage("influxdb-readonly-baseurl is required")
      )
    })


    cliParser.opt[String]("influxdb-readonly-user")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(readonlyUser = Some(value))) }
      .text("By default --influxdb-user")

    appendSimpleOptionalConfigRule[String]("influxdb.readonly.user") { (value, opts) =>
      influxDbOpts(opts)(_.copy(readonlyUser = Some(value)))
    }

    appendInfluxDbOptsCheck({
      opts => Either.cond(
        opts.readonlyUser.isDefined,
        (),
        ParsingError.withMessage("influxdb-readonly-user is required")
      )
    })


    cliParser.opt[String]("influxdb-readonly-password")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(readonlyPassword = Some(value))) }
      .text("By default --influxdb-password")

    appendSimpleOptionalConfigRule[String]("influxdb.readonly.password") { (value, opts) =>
      influxDbOpts(opts)(_.copy(readonlyPassword = Some(value)))
    }

    appendInfluxDbOptsCheck({
      opts => Either.cond(
        opts.readonlyPassword.isDefined,
        (),
        ParsingError.withMessage("influxdb-readonly-password is required")
      )
    })

    appendPostprocessor { opts =>
      Right(
        influxDbOpts(opts){ influxdbOpts =>
          influxdbOpts.copy(
            readonlyBaseUrl = influxdbOpts.readonlyBaseUrl orElse Some(influxdbOpts.baseUrl),
            readonlyUser = influxdbOpts.readonlyUser orElse influxdbOpts.user,
            readonlyPassword = influxdbOpts.readonlyPassword orElse influxdbOpts.password
          )
        }
      )
    }

  }
}
