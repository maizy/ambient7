package ru.maizy.ambient7.core.tests.config

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.{ Ambient7Options, DbOptions, UniversalConfigReader }
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
        appendCheck {opts => Either.cond(opts.mainDb.isDefined, (), IndexedSeq("db opts required")) }
      }
    }

    val requiredReader = new OptsRequiredReader()
    requiredReader.fillReader()

    allOptionalReader.readAppConfig(IndexedSeq.empty) shouldBe Right(Ambient7Options())
    requiredReader.readAppConfig(IndexedSeq.empty) shouldBe Left(IndexedSeq("db opts required"))
  }

  it should "required config if needed" in {
    class ParserWithConfig(required: Boolean) extends UniversalConfigReader {
      override def appName: String = "test app"

      override def fillReader(): Unit = {
        fillConfigOptions(required)
        ()
      }
    }

    val requiredConfigReader = new ParserWithConfig(required = true)
    requiredConfigReader.fillReader()

    val optionalConfigReader = new ParserWithConfig(required = false)
    optionalConfigReader.fillReader()

    requiredConfigReader.isConfigEnabled shouldBe true
    optionalConfigReader.isConfigEnabled shouldBe true

    requiredConfigReader.readAppConfig(IndexedSeq.empty) shouldBe Left(IndexedSeq("config path is required"))
    requiredConfigReader.readAppConfig(IndexedSeq("-u", "some")) shouldBe Left(IndexedSeq("config path is required"))

    optionalConfigReader.readAppConfig(IndexedSeq.empty) shouldBe Right(Ambient7Options())

  }

}
