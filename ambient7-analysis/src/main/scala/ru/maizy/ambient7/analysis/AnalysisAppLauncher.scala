package ru.maizy.ambient7.analysis

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.analysis.command.{ AggregateCo2Command, InitDbCommand, NotificationCommand }
import ru.maizy.ambient7.core.config.ParsingError


object AnalysisAppLauncher extends App {

  AnalysisConfigReader.readAppConfig(args) match {
    case Left(parsingError) =>
      analysisLogger.error(ParsingError.formatErrorsForLog(parsingError))
      Console.err.println(ParsingError.formatErrorsAndUsage(parsingError))
      System.exit(2)

    case Right(opts) if opts.analysisSpecificOptions.isEmpty =>
      Console.err.println("unknown command")
      System.exit(2)

    case Right(opts) =>
      opts.analysisSpecificOptions.map(_.command).getOrElse("") match {
        case Some("init-db") =>
          opts.mainDb match {
            case Some(dbOpts) =>
              val res = InitDbCommand.run(dbOpts.url.getOrElse(""), dbOpts.user, dbOpts.password)
              System.exit(res.systemExitCode)
            case None =>
              analysisLogger.error("db opts required")
              System.exit(2)
          }

        case Some("aggregate-co2") =>
          val res = AggregateCo2Command.run(opts)
          System.exit(res.systemExitCode)

        case Some("notifications") =>
          NotificationCommand.run(opts)

        case _ =>
          Console.err.println("Unknown command")
          System.exit(2)
      }

  }
}
