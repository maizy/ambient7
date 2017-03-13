package ru.maizy.ambient7.analysis

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.core.config.options.AnalysisSpecificOptions
import ru.maizy.ambient7.core.config.reader.{ DevicesConfigReader, InfluxDbConfigReader, MainDbConfigReader }
import ru.maizy.ambient7.core.config.reader.{ NotificationsConfigReader, UniversalConfigReader }

object AnalysisConfigReader
  extends UniversalConfigReader
  with DevicesConfigReader
  with InfluxDbConfigReader
  with MainDbConfigReader
  with NotificationsConfigReader
{
  override def appName: String = "java -jar ambient7-analysis.jar"

  private def setCommand(opts: Ambient7Options, command: String): Ambient7Options = {
    val existingOpts = opts.analysisSpecificOptions.getOrElse(AnalysisSpecificOptions())
    opts.copy(analysisSpecificOptions = Some(existingOpts.copy(command = Some(command))))
  }

  override def fillReader(): Unit = {
    fillDevicesOptions()
    fillConfigOptions(requireConfig = true)
    fillInfluxDbOptions()
    fillDbOptions()
    fillNotificationsOptions()

    cliParser.cmd("aggregate-co2")
      .action { (_, opts) => setCommand(opts, "aggregate-co2") }
      .text("\tcompute hourly co2 levels report")

    cliParser.cmd("init-db")
      .action { (_, opts) => setCommand(opts, "init-db") }
      .text("\tinitialize or upgrade db")

    cliParser.cmd("notifications")
      .action { (_, opts) => setCommand(opts, "notifications") }
      .text("\twatch for data & send notifications (stand alone mode)")

    ()
  }

  fillReader()
}
