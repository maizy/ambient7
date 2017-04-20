package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.core.config.options.WebAppSpecificOptions
import ru.maizy.ambient7.core.config.reader.{ DevicesConfigReader, MainDbConfigReader, UniversalConfigReader }

object WebAppConfigReader
  extends UniversalConfigReader
  with MainDbConfigReader
  with DevicesConfigReader
{
  import UniversalConfigReader._

  val DEFAULT_PORT = 22480

  private def webAppOpts(opts: Ambient7Options)(
      save: WebAppSpecificOptions => WebAppSpecificOptions): Ambient7Options =
  {
    val currentOpts = opts.webAppSpecificOptions.getOrElse(WebAppSpecificOptions())
    opts.copy(webAppSpecificOptions = Some(save(currentOpts)))
  }

  override def appName: String = "java -jar ambient7-webapp.jar"

  override def fillReader(): Unit = {
    fillConfigOptions(requireConfig = true)
    fillDbOptions()
    fillDevicesOptions()

    cliParser.opt[Int]("port")
      .abbr("p")
      .valueName(s"<$DEFAULT_PORT>")
      .action { (value, opts) => webAppOpts(opts)(_.copy(port = value)) }
      .text(s"web app listen port")

    appendSimpleOptionalConfigRule[Int]("webapp.port") { (value, opts) =>
      webAppOpts(opts)(_.copy(port = value))
    }

    appendCheck { opts =>
      if (opts.webAppSpecificOptions.isEmpty) {
        failure("webapp options are required")
      } else if (opts.webAppSpecificOptions.exists(_.port <= 0)) {
        failure("webapp port should be positive int")
      } else {
        success
      }
    }
  }

  fillReader()
}
