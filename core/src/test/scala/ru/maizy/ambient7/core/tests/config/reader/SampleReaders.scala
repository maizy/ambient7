package ru.maizy.ambient7.core.tests.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.DbOptions
import ru.maizy.ambient7.core.config.reader.UniversalConfigReader

trait SampleReaders {

  class ReaderWithSimpleOpts extends UniversalConfigReader {
    override def appName: String = "test app"

    override def fillReader(): Unit = {
      cliParser.opt[String]("option")
        .abbr("o")
        .action { (value, opts) => opts.copy(mainDb = Some(DbOptions(url = Some(value)))) }

      ()
    }
  }

  class ReaderWithSimpleOptsAndPostprocessors extends ReaderWithSimpleOpts {
    override def fillReader(): Unit = {
      super.fillReader()

      appendPostprocessor { opts =>
        val mainDbOpts = opts.mainDb.getOrElse(DbOptions())
        Right(opts.copy(mainDb = Some(mainDbOpts.copy(password = mainDbOpts.url.getOrElse(":/")))))
      }

      appendPostprocessor { opts =>
        val mainDbOpts = opts.mainDb.getOrElse(DbOptions())
        Right(opts.copy(mainDb = Some(mainDbOpts.copy(user = mainDbOpts.password))))
      }
    }
  }

  class ReaderWithConfig(requireConfig: Boolean) extends UniversalConfigReader {
    override def appName: String = "test app"

    override def fillReader(): Unit = {
      fillConfigOptions(requireConfig)
      cliParser.opt[String]("option")
        .abbr("o")
        .action { (value, opts) => opts.copy(mainDb = Some(DbOptions(url = Some(value)))) }

      ()
    }
  }

  class ReaderWithConfigRule(requireConfig: Boolean) extends UniversalConfigReader {
    override def appName: String = "test app"
    override def fillReader(): Unit = {
      fillConfigOptions(requireConfig)
      appendConfigRule { (config, opts) =>
        Right(opts.copy(mainDb = Some(DbOptions(url=Some(config.getString("sample.string"))))))
      }
      ()
    }
  }

  class ReaderWithSimpleConfigRule(path: String) extends UniversalConfigReader {
    override def appName: String = "test app"
    override def fillReader(): Unit = {
      fillConfigOptions(requireConfig = true)
      appendSimpleConfigRule[String](path) { (value, opts) =>
        opts.copy(mainDb = Some(DbOptions(url = Some(value))))
      }
      ()
    }
  }

  class ReaderWithSimpleOptionalConfigRule(path: String) extends UniversalConfigReader {
    override def appName: String = "test app"
    override def fillReader(): Unit = {
      fillConfigOptions(requireConfig = true)
      appendSimpleOptionalConfigRule[String](path) { (value, opts) =>
        opts.copy(mainDb = Some(DbOptions(url = Some(value))))
      }
      ()
    }
  }

  class ReaderConfigRuleAndCliOption extends ReaderWithConfigRule(requireConfig = true) {
    override def fillReader(): Unit = {
      super.fillReader()
      cliParser.opt[String]("some")
        .action { (value, opts) => opts.copy(mainDb = Some(DbOptions(url = Some(value)))) }
      ()
    }
  }
}
