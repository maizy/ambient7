package ru.maizy.ambient7.analysis

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import ru.maizy.ambient7.analysis.command.{ InitDbCommand, ReturnStatus }


object AnalysisApp extends App {

  val logger = Logger(LoggerFactory.getLogger("ru.maizy.ambient7.analysis"))

  OptionParser.parse(args) match {
    case None =>
      logger.error("Wrong app options, exiting")
      System.exit(2)

    case Some(opts) =>
      val res: ReturnStatus = opts.command match {
        case Some("init-db") =>
          InitDbCommand.run(opts.dbUrl, opts.dbUser, opts.dbPassword)

        case Some("aggregate-co2") =>
          // FIXME
          ReturnStatus.success

        case _ =>
          println("Unknown command")
          ReturnStatus(2)
      }
      System.exit(res.systemExitCode)

  }
}
