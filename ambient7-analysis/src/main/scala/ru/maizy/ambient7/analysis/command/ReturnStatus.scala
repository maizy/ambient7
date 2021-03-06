package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
case class ReturnStatus(systemExitCode: Int)

object ReturnStatus {
  def success: ReturnStatus = ReturnStatus(0)
  def paramsError: ReturnStatus = ReturnStatus(1)
  def computeError: ReturnStatus = ReturnStatus(2)
}
