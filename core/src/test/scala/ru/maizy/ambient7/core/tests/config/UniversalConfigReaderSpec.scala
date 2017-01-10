package ru.maizy.ambient7.core.tests.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.{ Ambient7Options, DbOptions, ParsingError, UniversalConfigReader }
import ru.maizy.ambient7.core.tests.BaseSpec

class UniversalConfigReaderSpec extends BaseSpec {

  class ReaderWithSimpleOpts extends UniversalConfigReader {
    override def appName: String = "test app"

    override def fillReader(): Unit = {
      cliParser.opt[String]("option")
        .abbr("o")
        .action { (value, opts) => opts.copy(mainDb = Some(DbOptions(url = Some(value)))) }

      ()
    }
  }

  class ParserWithConfig(required: Boolean) extends UniversalConfigReader {
    override def appName: String = "test app"

    override def fillReader(): Unit = {
      fillConfigOptions(required)
      cliParser.opt[String]("option")
        .abbr("o")
        .action { (value, opts) => opts.copy(mainDb = Some(DbOptions(url = Some(value)))) }

      ()
    }
  }

  "UniversalConfigReader" should "allow to add cli opts" in {
    val reader = new ReaderWithSimpleOpts()
    reader.fillReader()

    val expected = Right(Ambient7Options(mainDb = Some(DbOptions(url = Some("value")))))

    reader.readAppConfig(IndexedSeq("-o", "value")) shouldBe expected
    reader.readAppConfig(IndexedSeq("--option", "value")) shouldBe expected
    reader.readAppConfig(IndexedSeq("--option=value")) shouldBe expected
  }

  it should "return some itself properties" in {
    val reader = new ReaderWithSimpleOpts()
    reader.fillReader()

    reader.appName shouldBe "test app"
    reader.isCliOptionsEnabled shouldBe true
    reader.isConfigEnabled shouldBe false
  }

  it should "return error for failed check" in {
    val allOptionalReader = new ReaderWithSimpleOpts()
    allOptionalReader.fillReader()

    class OptsRequiredReader extends ReaderWithSimpleOpts {
      override def fillReader(): Unit = {
        super.fillReader()
        appendCheck {opts => Either.cond(opts.mainDb.isDefined, (), ParsingError.withMessage("db opts required")) }
      }
    }

    val requiredReader = new OptsRequiredReader()
    requiredReader.fillReader()

    allOptionalReader.readAppConfig(IndexedSeq.empty) shouldBe Right(Ambient7Options())

    requiredReader.readAppConfig(IndexedSeq.empty) shouldHaveUsageAndErrorMessage "db opts required"
  }

  it should "required config if needed" in {
    val configReader = new ParserWithConfig(required = true)
    configReader.fillReader()

    configReader.isConfigEnabled shouldBe true

    configReader.readAppConfig(IndexedSeq.empty) shouldHaveUsageAndErrorMessage "config path is required"
    configReader.readAppConfig(IndexedSeq("-o", "some")) shouldHaveUsageAndErrorMessage "config path is required"

  }

  it should "not required config if it's options" in {
    val configReader = new ParserWithConfig(required = false)
    configReader.fillReader()

    configReader.isConfigEnabled shouldBe true

    configReader.readAppConfig(IndexedSeq.empty) shouldBe Right(Ambient7Options())
    configReader.readAppConfig(IndexedSeq("-o", "value")) shouldBe
      Right(Ambient7Options(mainDb = Some(DbOptions(url = Some("value")))))
  }

  it should "show usage" in {
    val reader = new ReaderWithSimpleOpts()
    reader.fillReader()

    reader.readAppConfig(IndexedSeq("-h")) shouldHaveOnlyUsageWithoutErrors()
    reader.readAppConfig(IndexedSeq("--help")) shouldHaveOnlyUsageWithoutErrors()
    reader.readAppConfig(IndexedSeq("--help", "-o=value")) shouldHaveOnlyUsageWithoutErrors()
  }

  it should "show usage if config required" in {
    val reader = new ParserWithConfig(required = true)
    reader.fillReader()

    reader.readAppConfig(IndexedSeq("-h")) shouldHaveOnlyUsageWithoutErrors()
    reader.readAppConfig(IndexedSeq("-h", "--config=bad.config")) shouldHaveOnlyUsageWithoutErrors()
  }

  implicit class ShouldHaveUsage(result: UniversalConfigReader.ParseResult) {
    def shouldHaveUsage(): Unit = {
      result should be ('left)
      result.left.get.usage shouldBe defined
      result.left.get.usage.get.length should be > 0
      result.left.get.usage.get.toLowerCase should not contain "usage"
      ()
    }
  }

  implicit class ShouldHaveUsageAndErrorMessage(result: UniversalConfigReader.ParseResult)
    extends ShouldHaveUsage(result)
  {
    def shouldHaveUsageAndErrorMessage(message: String): Unit = {
      result should be ('left)
      shouldHaveUsage()
      result.left.get.messages shouldBe IndexedSeq(message)
      ()
    }
  }

  implicit class ShouldHaveOnlyUsageWithoutErrors(result: UniversalConfigReader.ParseResult)
    extends ShouldHaveUsage(result)
  {
    def shouldHaveOnlyUsageWithoutErrors(): Unit = {
      result should be ('left)
      shouldHaveUsage()
      result.left.get.messages shouldBe IndexedSeq.empty
      ()
    }
  }

}
