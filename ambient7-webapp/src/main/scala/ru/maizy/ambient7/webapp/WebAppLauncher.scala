package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
object WebAppLauncher extends App {

  val config = WebAppConfigReader
  config.fillReader()
  val eitherAppConfig = config.readAppConfig(args.toIndexedSeq)
  println(eitherAppConfig)

  // TODO: launch jetty app (merge with JettyLauncher)
}
