package ru.maizy.ambient7.core.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.io.File
import java.nio.file.Files
import scala.annotation.tailrec
import scopt.OptionParser

object UniversalConfigReader {
  type CheckResult = Either[ParsingError, Unit]
  type Check = Ambient7Options => CheckResult
  type ParseResult = Either[ParsingError, Ambient7Options]
  type Postprocessor = Ambient7Options => ParseResult
}

trait UniversalConfigReader {
  import UniversalConfigReader._

  private var checks_ = List[Check]()
  private var postprocessors_ = List[Postprocessor]()

  private var isConfigEnabled_ = false
  // private var isConfigRequired_ = false

  private val isCliOptionsEnabled_ = true

  class SilentOptionParser[T](programmName: String) extends OptionParser[T](programmName) {
    private var savedErrors: IndexedSeq[String] = IndexedSeq.empty
    private var savedWarning: IndexedSeq[String] = IndexedSeq.empty

    override def showUsage(): Unit = {}
    override def showUsageAsError(): Unit = {}

    override def reportError(msg: String): Unit = {
      savedErrors = savedErrors :+ msg
      ()
    }

    override def reportWarning(msg: String): Unit = {
      savedWarning = savedWarning :+ msg
      ()
    }

    def appendUsageToParserError(parser: ParsingError): ParsingError = {
      val pattern = """(?ims)usage:\s+(.*)""".r
      usage match {
        case pattern(content) => parser.appendUsage(content)
        case _ => parser.appendUsage(usage)
      }
    }

  }

  protected val cliParser = new SilentOptionParser[Ambient7Options](appName) {
    opt[Unit]("help")
      .abbr("h")
      .action((_, opts) => opts.copy(showHelp = true))

    override def showUsageOnError: Boolean = true
  }

  protected val configPathCliParser = new SilentOptionParser[Ambient7Options](appName) {
    override val errorOnUnknownArgument = false

    override def reportError(msg: String): Unit = {}

    override def reportWarning(msg: String): Unit = {}

    opt[Unit]("help")
      .abbr("h")
      .action((_, opts) => opts.copy(showHelp = true))
  }

  protected def fillConfigOptions(requireUniversalConfig: Boolean = false): Unit = {
    isConfigEnabled_ = true
    // isConfigRequired_ = requireUniversalConfig

    def addConfigOption(parser: OptionParser[Ambient7Options], required: Boolean = false): Unit = {
      val opt = parser.opt[File]("config")
        .abbr("c")
        .action { (value, opts) => opts.copy(universalConfigPath = Some(value.toPath.toAbsolutePath)) }
        .text(s"path to universal ambient7 config")

      if (required) {
        opt.required()
        appendCheck { opts =>
          Either.cond(opts.universalConfigPath.isDefined, (), ParsingError.withMessage("config path is required"))
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

  private def processHelpOption(result: ParseResult): ParseResult = {
    result match {
      // show usage if --help option exists
      case Right(opts) if opts.showHelp => Left(ParsingError())
      case _ => result
    }
  }

  private def parseConfig(opts: Ambient7Options, args: IndexedSeq[String]): ParseResult = {
    configLogger.debug("config enabled")
    val optsWithConfigPath = configPathCliParser.parse(args, opts).getOrElse(opts)
    var parseResult: ParseResult = Right(optsWithConfigPath)
    parseResult = processHelpOption(parseResult)
    parseResult.right.flatMap { optsWithConfigPath =>
      optsWithConfigPath.universalConfigPath match {
        case Some(universalConfigPath) =>
          if (Files.isReadable(universalConfigPath)) {
            configLogger.info(s"parse config from $universalConfigPath")

            // FIXME: implements

            Right(optsWithConfigPath)
          } else {
            val message = s"unable to read config from $universalConfigPath"
            configLogger.error(message)
            Left(ParsingError.withMessage(message))
          }

        case _ =>
          configLogger.info("universal config path not defined")
          Right(opts)
      }
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
    def untilError(xs: List[Check]): Option[ParsingError] = {
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
    eitherAppConfig = processHelpOption(eitherAppConfig)
    eitherAppConfig = eitherAppConfig.right.flatMap(opts => checkConfig(opts))
    eitherAppConfig match {
      case config@Right(_) => config
      case Left(parsingError) if isCliOptionsEnabled => Left(cliParser.appendUsageToParserError(parsingError))
      case error@Left(_) => error
    }
  }

}


