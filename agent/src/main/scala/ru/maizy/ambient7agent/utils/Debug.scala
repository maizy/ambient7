package ru.maizy.ambient7agent.utils

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
object Debug {
  def prettyByteArray(bytes: Array[Byte]): String = {
    bytes.zipWithIndex.map{ case (b, ind) => s"$ind. ${b.formatted("%02x")}" }.mkString("\n")
  }
}
