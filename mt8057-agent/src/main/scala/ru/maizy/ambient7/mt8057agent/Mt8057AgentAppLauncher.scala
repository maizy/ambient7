package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

object Mt8057AgentAppLauncher extends App {

  val eitherAppConfig = Mt8057ConfigReader.readAppConfig(args.toIndexedSeq)
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
      println(s"co2 device: ${opts.selectedCo2Device}")
      println(s"all known co2 devices: ${opts.devices.map(_.co2Devices)}")
  }
}
