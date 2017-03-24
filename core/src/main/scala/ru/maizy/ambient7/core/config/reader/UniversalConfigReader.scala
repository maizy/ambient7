package ru.maizy.ambient7.core.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.io.File
import java.nio.file.{ Files, Path }
import scala.annotation.tailrec
import scala.util.{ Failure, Try, Success }
import com.typesafe.config.{ Config, ConfigFactory }
import configs.Configs
import scopt.OptionParser
import ru.maizy.ambient7.core.config.helper.ConfigRuleOps.IfSuccessOp
import ru.maizy.ambient7.core.config.{ Ambient7Options, ParsingError, configLogger }

object UniversalConfigReader {
  type CheckResult = Either[ParsingError, Unit]
  type Check = Ambient7Options => CheckResult
  type ParseResult = Either[ParsingError, Ambient7Options]
  type Postprocessor = Ambient7Options => ParseResult
  type ConfigRule = (Config, Ambient7Options) => ParseResult

  val success: CheckResult = Right(())
  def failure(message: String): CheckResult = Left(ParsingError.withMessage(message))
  def failure(messages: IndexedSeq[String]): CheckResult = Left(ParsingError.withMessages(messages))
}

trait UniversalConfigReader {
  import UniversalConfigReader._

  private var checks_ = List[Check]()
  private var postprocessors_ = List[Postprocessor]()

  private var isConfigEnabled_ = false
  private var isConfigRequired_ = false
  private var configRules_ = List[ConfigRule]()

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

    def errorsAndWarnings: IndexedSeq[String] =
      savedErrors ++ savedWarning

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

  protected def fillConfigOptions(requireConfig: Boolean = false): Unit = {
    isConfigEnabled_ = true
    isConfigRequired_ = requireConfig

    def addConfigOption(parser: OptionParser[Ambient7Options], required: Boolean = false): Unit = {
      parser.opt[File]("config")
        .abbr("c")
        .action { (value, opts) => opts.copy(universalConfigPath = Some(value.toPath.toAbsolutePath)) }
        .text(s"path to universal ambient7 config")

      if (required) {
        appendCheck { opts =>
          Either.cond(opts.universalConfigPath.isDefined, (), ParsingError.withMessage("config path is required"))
        }
      }

      ()
    }

    addConfigOption(configPathCliParser)
    addConfigOption(cliParser, required = requireConfig)
  }

  def isConfigEnabled: Boolean = isConfigEnabled_

  def isConfigRequired: Boolean = isConfigRequired_

  def isCliOptionsEnabled: Boolean = isCliOptionsEnabled_

  def appName: String

  def fillReader(): Unit

  def checks: List[Check] = checks_.reverse

  protected def appendCheck(check: Check): Unit = checks_ = check :: checks_

  def postprocessors: List[Postprocessor] = postprocessors_.reverse

  protected def appendPostprocessor(postprocessor: Postprocessor): Unit =
    postprocessors_ = postprocessor :: postprocessors_

  protected def appendConfigRule(rule: ConfigRule): Unit =
    configRules_ = rule :: configRules_

  protected def appendSimpleConfigRule[T](configPath: String)(
      saveValue: (T, Ambient7Options) => Ambient7Options)(implicit reader: Configs[T]): Unit =
  {
    appendConfigRule { (config, opts) =>
      Configs.apply[T].get(config, configPath).ifSuccess(value => saveValue(value, opts))
    }
  }

  protected def appendSimpleOptionalConfigRule[T](configPath: String)(
      save: (T, Ambient7Options) => Ambient7Options)(implicit reader: Configs[Option[T]]): Unit =
  {
    appendSimpleConfigRule[Option[T]](configPath) { (mayBeValue, opts) =>
      mayBeValue match {
        case Some(value) => save(value, opts)
        case None => opts
      }
    }
  }

  def configRules: List[ConfigRule] = configRules_.reverse

  private def processHelpOption(result: ParseResult): ParseResult = {
    result match {
      // show usage if --help option exists
      case Right(opts) if opts.showHelp => Left(ParsingError())
      case _ => result
    }
  }

  private def safeLoadConfig(universalConfigPath: Path): Either[IndexedSeq[String], Config] = {
    Try(ConfigFactory.parseFile(universalConfigPath.toFile)) match {
      case Failure(exception) => Left(IndexedSeq(s"reading config error ${exception.getMessage}"))
      case Success(config) => Right(config)
    }
  }

  private def processCliParser(
      parser: SilentOptionParser[Ambient7Options], args: IndexedSeq[String], opts: Ambient7Options): ParseResult =
  {
    parser.parse(args, opts) match {
      case None => Left(ParsingError.withMessages(parser.errorsAndWarnings))
      case Some(resOpts) => Right(resOpts)
    }
  }

  private def parseConfig(opts: Ambient7Options, args: IndexedSeq[String]): ParseResult = {
    configLogger.debug("config enabled")
    var parseResult: ParseResult = processCliParser(configPathCliParser, args, opts)
    parseResult = processHelpOption(parseResult)
    parseResult.right.flatMap { optsWithConfigPath =>
      optsWithConfigPath.universalConfigPath match {
        case Some(configPath) =>
          if (Files.isReadable(configPath)) {
            configLogger.info(s"parse config from $configPath")
            safeLoadConfig(configPath)
              .left.map(ParsingError.withMessages(_))
              .right.flatMap { config =>
                val configRulesApplingResult = configRules.foldLeft[ParseResult](Right(optsWithConfigPath)) {
                  case (error@Left(_), _) => error
                  case (res@Right(_), rule) => res.right.flatMap(opts => rule(config, opts))
                }
                configRulesApplingResult
              }
          } else {
            val message = s"unable to read config from $configPath"
            configLogger.error(message)
            Left(ParsingError.withMessage(message))
          }
        case _ =>
          configLogger.info("universal config path not defined")
          Right(opts)
      }
    }
  }

  private def parseCliOptions(args: IndexedSeq[String], opts: Ambient7Options): ParseResult =
    processCliParser(cliParser, args, opts)

  private def applyPostprocessors(result: ParseResult): ParseResult = {
    result.right.flatMap { opts =>
      postprocessors.foldLeft[ParseResult](Right(opts)) {
        case (error@Left(_), _) => error
        case (res@Right(_), processor) => res.right.flatMap(processor)
      }
    }
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
    eitherAppConfig = applyPostprocessors(eitherAppConfig)
    eitherAppConfig = eitherAppConfig.right.flatMap(opts => checkConfig(opts))
    eitherAppConfig match {
      case success@Right(_) => success
      case Left(parsingError) if isCliOptionsEnabled => Left(cliParser.appendUsageToParserError(parsingError))
      case error@Left(_) => error
    }
  }

  implicit class ErrorOrFlatmap[T, V](result: configs.Result[T]) {
    def errorOrFlatmap(map: T => Either[Seq[String], V]): Either[Seq[String], V] = {
      result.toEither match {
        case Right(r) => map(r)
        case Left(errors) => Left(errors.messages)
      }
    }
  }
}


