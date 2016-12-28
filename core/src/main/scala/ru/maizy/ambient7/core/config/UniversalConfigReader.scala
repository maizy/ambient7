package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.io.File
import java.nio.file.Files
import scala.annotation.tailrec
import scopt.OptionParser

trait UniversalConfigReader {

  type CheckResult = Either[IndexedSeq[String], Unit]
  type Check = Ambient7Options => CheckResult
  type ParseResult = Either[IndexedSeq[String], Ambient7Options]
  type Postprocessor = Ambient7Options => ParseResult

  private var checks_ = List[Check]()
  private var postprocessors_ = List[Postprocessor]()

  private var isConfigEnabled_ = false

  private val isCliOptionsEnabled_ = true

  protected val cliParser = new OptionParser[Ambient7Options](appName) {
    help("help")

    override def showUsageOnError: Boolean = true
  }

  protected val configPathCliParser = new OptionParser[Ambient7Options](appName) {
    override val errorOnUnknownArgument = false

    override def reportError(msg: String): Unit = {}

    override def reportWarning(msg: String): Unit = {}
  }

  protected def fillConfigOptions(requireUniversalConfig: Boolean = false): Unit = {
    isConfigEnabled_ = true

    def addConfigOption(parser: OptionParser[Ambient7Options], required: Boolean = false): Unit = {
      val opt = parser.opt[File]("config")
        .abbr("c")
        .action { (value, opts) => opts.copy(universalConfigPath = Some(value.toPath.toAbsolutePath)) }
        .text(s"path to universal ambient7 config")

      if (required) {
        opt.required()
        appendCheck { opts =>
          Either.cond(opts.universalConfigPath.isDefined, (), IndexedSeq("config path is required"))
        }
      }

      ()
    }

    addConfigOption(configPathCliParser)
    addConfigOption(cliParser, required = requireUniversalConfig)
  }

  def isConfigEnabled: Boolean = isConfigEnabled_

  def isCliOptionsEnabled: Boolean = isCliOptionsEnabled_

  def appName: String

  def fillReader(): Unit

  def checks: List[Check] = checks_.reverse

  protected def appendCheck(check: Check): Unit = checks_ = check :: checks_

  def postprocessors: List[Postprocessor] = postprocessors_.reverse

  protected def appendPostprocessor(postprocessor: Postprocessor): Unit =
    postprocessors_ = postprocessor :: postprocessors_

  private def parseConfig(opts: Ambient7Options, args: IndexedSeq[String]): ParseResult = {
    configLogger.debug("config enabled")
    val optsWithConfigPath = configPathCliParser.parse(args, opts).getOrElse(opts)
    optsWithConfigPath.universalConfigPath match {
      case Some(universalConfigPath) =>
        if (Files.isReadable(universalConfigPath)) {
          configLogger.info(s"parse config from $universalConfigPath")

          // FIXME: implements

          Right(optsWithConfigPath)
        } else {
          val message = s"unable to read config from $universalConfigPath"
          configLogger.error(message)
          Left(IndexedSeq(message))
        }

      case _ =>
        configLogger.info("universal config path not defined")
        Right(opts)
    }
  }

  private def parseCliOptions(
      args: IndexedSeq[String], opts: Ambient7Options): ParseResult =
  {
    configLogger.debug("cli options enabled")
    Right(cliParser.parse(args, opts).getOrElse(opts))
  }

  private def checkConfig(opts: Ambient7Options): ParseResult =
  {
    @tailrec
    def untilError(xs: List[Check]): Option[IndexedSeq[String]] = {
      xs match {
        case Nil => None
        case check :: tail =>
          check(opts) match {
            case Left(errors) => Some(errors)
            case Right(_) => untilError(tail)
          }
      }
    }
    val firstError = untilError(checks)
    firstError match {
      case Some(errors) => Left(errors)
      case _ => Right(opts)
    }
  }

  def readAppConfig(args: IndexedSeq[String]): ParseResult = {
    var eitherAppConfig: ParseResult = Right(Ambient7Options())
    if (isConfigEnabled) {
      eitherAppConfig = eitherAppConfig.right.flatMap(opts => parseConfig(opts, args))
    }
    if (isCliOptionsEnabled) {
      eitherAppConfig = eitherAppConfig.right.flatMap(opts => parseCliOptions(args, opts))
    }
    eitherAppConfig.right.flatMap(opts => checkConfig(opts))
  }

}


