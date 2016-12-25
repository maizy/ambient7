package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.annotation.tailrec
import scopt.OptionParser

trait UniversalConfigParser {
  val parser = new OptionParser[Ambient7Options](appName) {}

  def fillUniversalConfigOptions(): Unit = {
    parser.opt[String]("config")
      .abbr("c")
      .action { (value, opts) => opts.copy(mainConfigPath = Some(value)) }
      .text(s"path to universal ambient7 config")
  }

  type Check = Option[Ambient7Options] => Either[String, Unit]

  private var checks_ = List[Check]()

  def appName: String

  def fillParser(): Unit = {
    fillUniversalConfigOptions()
  }

  def checks: List[Check] = checks_.reverse

  def appendCheck(check: Check): Unit = checks_ = check :: checks_

  def parse(args: Seq[String]): Either[IndexedSeq[String], Ambient7Options] = {

    val opts = parser.parse(args, Ambient7Options())

    // TODO: universal config parsing

    @tailrec
    def untilError(xs: List[Check]): Option[String] = {
      xs match {
        case Nil => None
        case check :: tail =>
          check(opts) match {
            case Left(error) => Some(error)
            case Right(_) => untilError(tail)
          }
      }
    }
    val firstError = untilError(checks)
    firstError match {
      case Some(error) => Left(IndexedSeq(error))
      case None if opts.isEmpty => Right(Ambient7Options())
      case _ => Right(opts.get)
    }
  }

}


