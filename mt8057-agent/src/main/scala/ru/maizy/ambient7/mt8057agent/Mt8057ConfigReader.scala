package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.io.File
import ru.maizy.ambient7.core.config.{ Ambient7Options, EnumerationMap, Mt8057AgentSpecificOptions, Writers }
import ru.maizy.ambient7.core.config.reader.{ DevicesConfigReader, InfluxDbConfigReader, UniversalConfigReader }
import ru.maizy.ambient7.core.data.DeviceType

object Mt8057ConfigReader
  extends UniversalConfigReader
  with DevicesConfigReader
  with InfluxDbConfigReader
{
  override def appName: String = "java -jar ambient7-mt8057-agent.jar"

  private def allInEnum(enum: EnumerationMap, values: Set[String]): Either[String, Unit] = {
    if (values.exists(!enum.valuesMap.contains(_))) {
      val allowed = enum.valuesMap.keys
      cliParser.failure(s"Some of '${ values.mkString(",") }' not in allowed values list (${ allowed.mkString(", ") })")
    } else {
      cliParser.success
    }
  }

  private def enumValues(enum: EnumerationMap) = enum.valuesMap.keys.mkString("|")

  private def mt8057AgentOpts(opts: Ambient7Options)(
      save: Mt8057AgentSpecificOptions => Mt8057AgentSpecificOptions): Ambient7Options =
  {
    val currentOpts = opts.mt8057AgentSpecificOptions.getOrElse(Mt8057AgentSpecificOptions())
    opts.copy(mt8057AgentSpecificOptions = Some(save(currentOpts)))
  }

  override def fillReader(): Unit = {
    fillConfigOptions()
    fillInfluxDbOptions(required = false)
    fillDevicesOptions()
    fillDeviceFromCliOptions(DeviceType.CO2)
    fillSelectedDeviceOptions()
    co2DeviceRequired()

    cliParser.opt[Seq[String]]("writers")
      .valueName { enumValues(Writers) }
      .validate { v => allInEnum(Writers,  v.toSet) }
      .action { (value, opts) => mt8057AgentOpts(opts)(_.copy(writers = value.toSet.map(Writers.valuesMap.apply))) }
      .text { "One or more writers divided by comma. But only one of console or interactive" }
      .required()

    cliParser.opt[Unit]("emulator")
      .text {
        "Emulator mode. Generate random data instead of device support." +
        "Do not use with the real DBs, because you will write a lots of crap to them."}
      .action { (_, opts) => mt8057AgentOpts(opts)(_.copy(useEmulator = true)) }

    cliParser.opt[File]("log-file")
      .action { (file, opts) => mt8057AgentOpts(opts)(_.copy(logFile = Some(file))) }

    cliParser.opt[Unit]("verbose")
      .action { (_, opts) => mt8057AgentOpts(opts)(_.copy(verboseLogging = true)) }

    appendCheck { opts =>
      val hasInfludbWriter = opts.mt8057AgentSpecificOptions.exists(_.writers.contains(Writers.InfluxDb))
      checkInfluxDbOpts(opts, required = hasInfludbWriter)
    }

    ()
  }

  fillReader()
}


