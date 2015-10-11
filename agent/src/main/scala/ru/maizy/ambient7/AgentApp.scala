package ru.maizy.ambient7

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
object AgentApp extends App {
  val app = new AgentAppJavaImpl
  app.executeExample()
}
