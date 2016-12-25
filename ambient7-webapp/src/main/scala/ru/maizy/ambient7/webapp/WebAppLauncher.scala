package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
object WebAppLauncher extends App {

  val config = WebAppConfigParser
  config.fillParser()
  val eitherAppConfig = config.parse(args.toSeq)
  println(eitherAppConfig)

  // TODO: launch jetty app
}
