package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

object WebAppLauncher extends App {

  val config = WebAppConfigReader
  config.fillReader()
  val eitherAppConfig = config.readAppConfig(args.toIndexedSeq)
  eitherAppConfig match {
    case Left(parsingError) =>
      // TODO: extract to core
      val sep = "\n  * "
      val errors = if (parsingError.messages.nonEmpty) {
        s"Errors:$sep${parsingError.messages.mkString(sep)}\n"
      } else {
        ""
      }
      val usage = parsingError.usage.map(u => s"Usage: $u").getOrElse("")
      val userResult = List(errors, usage).filterNot(_ == "").mkString("\n")
      Console.err.println(userResult)
    case Right(opts) =>
      println(s"Success: $opts")
  }

  // TODO: launch jetty app (merge with JettyLauncher)
}
