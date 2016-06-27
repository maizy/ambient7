package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case class ReturnStatus(systemExitCode: Int)

object ReturnStatus {
  def success: ReturnStatus = ReturnStatus(0)
}
