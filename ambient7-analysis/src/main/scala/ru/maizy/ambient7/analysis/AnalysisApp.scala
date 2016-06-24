package ru.maizy.ambient7.analysis

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger


object AnalysisApp extends App {

  val logger = Logger(LoggerFactory.getLogger("ru.maizy.ambient7.analysis"))

  OptionParser.parse(args) match {
    case None =>
      logger.error("Wrong app options, exiting")
      System.exit(2)

    case Some(opts) if opts.command.isEmpty =>
      logger.error("Unknown command, exiting")
      System.exit(2)

    case Some(opts) =>
      val command = opts.command.get
      logger.info(s"Command: $command")
      logger.info(s"DB url: ${opts.dbUrl}")

  }
}
