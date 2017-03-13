package ru.maizy.ambient7.core.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.options.InfluxDbOptions
import ru.maizy.ambient7.core.config.{ Ambient7Options, Defaults }


trait InfluxDbConfigReader extends UniversalConfigReader {

  import UniversalConfigReader._

  private def influxDbOpts(opts: Ambient7Options)(fill: InfluxDbOptions => InfluxDbOptions): Ambient7Options = {
    opts.copy(influxDb = Some(fill(opts.influxDb.getOrElse(InfluxDbOptions()))))
  }

  def fillInfluxDbOptions(required: Boolean = true): Unit = {

    cliParser.opt[String]("influxdb-baseurl")
      .valueName { s"<${Defaults.INFLUXDB_BASEURL}>" }
      .action { (value, opts) => influxDbOpts(opts)(_.copy(baseUrl = value)) }

    appendSimpleOptionalConfigRule[String]("influxdb.baseurl") { (value, opts) =>
      influxDbOpts(opts)(_.copy(baseUrl = value))
    }

    cliParser.opt[String]("influxdb-database")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(database = Some(value))) }

    appendSimpleOptionalConfigRule[String]("influxdb.database") { (value, opts) =>
      influxDbOpts(opts)(_.copy(database = Some(value)))
    }

    cliParser.opt[String]("influxdb-user")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(user = Some(value))) }

    appendSimpleOptionalConfigRule[String]("influxdb.user") { (value, opts) =>
      influxDbOpts(opts)(_.copy(user = Some(value)))
    }

    cliParser.opt[String]("influxdb-password")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(password = Some(value))) }

    appendSimpleOptionalConfigRule[String]("influxdb.password") { (value, opts) =>
      influxDbOpts(opts)(_.copy(password = Some(value)))
    }

    cliParser.opt[String]("influxdb-readonly-baseurl")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(readonlyBaseUrl = Some(value))) }
      .text("By default --influxdb-baseurl")

    appendSimpleOptionalConfigRule[String]("influxdb.readonly.baseurl") { (value, opts) =>
      influxDbOpts(opts)(_.copy(readonlyBaseUrl = Some(value)))
    }

    cliParser.opt[String]("influxdb-readonly-user")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(readonlyUser = Some(value))) }
      .text("By default --influxdb-user")

    appendSimpleOptionalConfigRule[String]("influxdb.readonly.user") { (value, opts) =>
      influxDbOpts(opts)(_.copy(readonlyUser = Some(value)))
    }

    cliParser.opt[String]("influxdb-readonly-password")
      .action { (value, opts) => influxDbOpts(opts)(_.copy(readonlyPassword = Some(value))) }
      .text("By default --influxdb-password")

    appendSimpleOptionalConfigRule[String]("influxdb.readonly.password") { (value, opts) =>
      influxDbOpts(opts)(_.copy(readonlyPassword = Some(value)))
    }

    appendCheck { opts =>
      checkInfluxDbOpts(opts, required)
    }

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

  def checkInfluxDbOpts(opts: Ambient7Options, required: Boolean): CheckResult = {
    opts.influxDb match {
      case None => failure("InfluxDb options are required")
      case Some(influxDbOpts) =>
        if (influxDbOpts.database.isDefined) {
          val parts = List(
            (influxDbOpts.user.isDefined, "influxdb-user is required"),
            (influxDbOpts.password.isDefined, "influxdb-password is required"),
            (influxDbOpts.readonlyBaseUrl.isDefined, "influxdb-readonly-baseurl is required"),
            (influxDbOpts.readonlyUser.isDefined, "influxdb-readonly-user is required"),
            (influxDbOpts.readonlyUser.isDefined, "influxdb-readonly-password is required")
          )

          val errors = for (
            (check, error) <- parts
            if !check
          ) yield error

          if (errors.nonEmpty) {
            failure(errors.toIndexedSeq)
          } else {
            success
          }
        } else if (!required) {
          success
        } else {
          failure("influxdb-database is required")
        }
    }
  }
}
