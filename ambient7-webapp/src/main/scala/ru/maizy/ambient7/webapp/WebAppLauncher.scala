package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
object WebAppLauncher extends App {

  val config = WebAppConfigReader
  config.fillReader()
  val eitherAppConfig = config.readAppConfig(args.toIndexedSeq)
  eitherAppConfig match {
    case Left(parsingError) =>
      // TODO: show usage and error if needed, extract to core function
      Console.err.println(
        s"\nUnable to launch app.\n\nErrors:\n * ${parsingError.messages.mkString("\n * ")}" +
        parsingError.usage.map(u => s"Usage: $u").getOrElse("")
      )
    case Right(opts) =>
      println(s"Success: $opts")
  }

  // TODO: launch jetty app (merge with JettyLauncher)
}
