package ru.maizy.ambient7.core.tests.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.nio.file.Paths
import ru.maizy.ambient7.core.config.options.DbOptions
import ru.maizy.ambient7.core.config.reader.UniversalConfigReader
import ru.maizy.ambient7.core.config.{ Ambient7Options, ParsingError }
import ru.maizy.ambient7.core.tests.BaseSpec

class UniversalConfigReaderSpec extends BaseSpec with SampleReaders {

  private val sampleConfigPath = getResourcePathString("sample-config.conf")

  "UniversalConfigReader" should "allow to add cli opts" in {
    val reader = new ReaderWithSimpleOpts()
    reader.fillReader()

    val expected = Right(Ambient7Options(mainDb = Some(DbOptions(url = Some("value")))))

    reader.readAppConfig(IndexedSeq("-o", "value")) shouldBe expected
    reader.readAppConfig(IndexedSeq("--option", "value")) shouldBe expected
    reader.readAppConfig(IndexedSeq("--option=value")) shouldBe expected
  }

  it should "apply postprocessors" in {
    val reader = new ReaderWithSimpleOptsAndPostprocessors()
    reader.fillReader()

    val expected = Right(Ambient7Options(
      mainDb = Some(DbOptions(url = Some("value"), user = "value", password = "value"))
    ))
    reader.readAppConfig(IndexedSeq("-o", "value")) shouldBe expected
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
    val configReader = new ReaderWithConfig(requireConfig = true)
    configReader.fillReader()

    configReader.isConfigEnabled shouldBe true

    configReader.readAppConfig(IndexedSeq.empty) shouldHaveUsageAndErrorMessage "config path is required"
    configReader.readAppConfig(IndexedSeq("-o", "some")) shouldHaveUsageAndErrorMessage "config path is required"

  }

  it should "not required config if it's options" in {
    val configReader = new ReaderWithConfig(requireConfig = false)
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
    val reader = new ReaderWithConfig(requireConfig = true)
    reader.fillReader()

    reader.readAppConfig(IndexedSeq("-h")) shouldHaveOnlyUsageWithoutErrors()
    reader.readAppConfig(IndexedSeq("-h", "--config=bad.config")) shouldHaveOnlyUsageWithoutErrors()
  }

  it should "allow add config rule" in {

    val readerRequired = new ReaderWithConfigRule(requireConfig = true)
    val readerOptional = new ReaderWithConfigRule(requireConfig = false)

    for (reader <- Seq(readerOptional, readerRequired)) {
      reader.fillReader()
      reader.readAppConfig(IndexedSeq("--config", sampleConfigPath)) shouldBe
        Right(
          Ambient7Options(
            universalConfigPath = Some(Paths.get(sampleConfigPath)),
            mainDb = Some(DbOptions(url = Some("abcd")))
          )
        )
    }
  }

  it should "allow add simple config rule" in {

    val reader = new ReaderWithSimpleConfigRule("sample.string")
    reader.fillReader()

    val readerWithNonExistingConfig = new ReaderWithSimpleConfigRule("sample.array")
    readerWithNonExistingConfig.fillReader()

    reader.readAppConfig(IndexedSeq("--config", sampleConfigPath)) shouldBe
      Right(
        Ambient7Options(
          universalConfigPath = Some(Paths.get(sampleConfigPath)),
          mainDb = Some(DbOptions(url = Some("abcd")))
        )
      )

    readerWithNonExistingConfig.readAppConfig(IndexedSeq("--config", sampleConfigPath)) should be ('left)
  }

  it should "allow add simple optional config rule" in {

    val reader = new ReaderWithSimpleOptionalConfigRule("sample.string")
    reader.fillReader()

    val readerWithNonExistingConfig = new ReaderWithSimpleOptionalConfigRule("unknown.string")
    readerWithNonExistingConfig.fillReader()

    reader.readAppConfig(IndexedSeq("--config", sampleConfigPath)) should be ('right)
    readerWithNonExistingConfig.readAppConfig(IndexedSeq("--config", sampleConfigPath)) should be ('right)
  }

  it should "cli opt should overwrite config rule" in {
    val reader = new ReaderConfigRuleAndCliOption
    reader.fillReader()

    reader.readAppConfig(IndexedSeq("--config", sampleConfigPath)) shouldBe
      Right(
        Ambient7Options(
          universalConfigPath = Some(Paths.get(sampleConfigPath)),
          mainDb = Some(DbOptions(url = Some("abcd")))
        )
      )

    reader.readAppConfig(IndexedSeq("--config", sampleConfigPath, "--some=zxy")) shouldBe
      Right(
        Ambient7Options(
          universalConfigPath = Some(Paths.get(sampleConfigPath)),
          mainDb = Some(DbOptions(url = Some("zxy")))
        )
      )
  }

  it should "return error if config type missmatched" in {
    val readers = Seq(
      new ReaderWithSimpleOptionalConfigRule("sample.array"),
      new ReaderWithSimpleConfigRule("sample.array")
    )

    for (reader <- readers) {
      reader.fillReader()
      reader.readAppConfig(IndexedSeq("--config", sampleConfigPath)) shouldHaveUsageAndErrorMessageContains
        "sample.array has type LIST rather than STRING"
    }

  }

  it should "interpret cli parser errors as errors" in {
    val reader = new ReaderWithSimpleRequiredOpts
    reader.fillReader()

    reader.readAppConfig(IndexedSeq.empty) shouldHaveUsageAndErrorMessage "Missing option --option"
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

  implicit class ShouldHaveUsageAndErrorMessageContains(result: UniversalConfigReader.ParseResult)
    extends ShouldHaveUsage(result)
  {
    def shouldHaveUsageAndErrorMessageContains(messageSubstring: String): Unit = {
      result should be ('left)
      shouldHaveUsage()
      result.left.get.messages should have length 1
      result.left.get.messages(0) should include(messageSubstring)
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
