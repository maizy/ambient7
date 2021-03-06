package ru.maizy.ambient7.core.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.annotation.tailrec
import com.typesafe.config.Config
import ru.maizy.ambient7.core.config.{ Ambient7Options, Defaults, FromCliDevice, ParsingError }
import ru.maizy.ambient7.core.data.{ AgentTags, Co2Agent, Co2Device, Device, DeviceType, Devices }

object DevicesConfigReader {

  private case class DeviceConfigModel(
      id: String,
      agentName: String,
      agentTags: Option[String] = None,
      model: String
  )

}

trait DevicesConfigReader extends UniversalConfigReader {

  import UniversalConfigReader._
  import DevicesConfigReader._
  import configs.syntax._

  def fillDeviceFromCliOptions(deviceType: DeviceType.Type): Unit = {

    cliParser.opt[String]("agent-name")
      .valueName { Defaults.INFLUXDB_AGENT_NAME }
      .action { (value, opts) => cliDeviceOpts(opts, deviceType)(_.copy(agent = value)) }

    val saveTags = (value: String, opts: Ambient7Options) =>
      cliDeviceOpts(opts, deviceType)(_.copy(tags = AgentTags(value)))

    val validateTags = (value: String) =>
      AgentTags.tryParseFromString(value) match {
        case Left(error) => cliParser.failure(error)
        case Right(_) => cliParser.success
      }

    cliParser.opt[String]("agent-tags")
      .valueName { "position=outdoor,altitude=200,some=val\\,ue" }
      .validate(validateTags)
      .action(saveTags)
      .text { "Any additional InfluxDB record tags"}

    appendCheck { opts =>
      if (opts.fromCliDevice.isDefined && opts.fromCliDevice.exists(_.deviceType.isEmpty)) {
        failure("device type is required")
      } else {
        success
      }
    }

    ()
  }

  def fillDevicesOptions(): Unit = {
    appendConfigRule { (config, opts) =>
      config.get[Option[List[Config]]]("devices").toEither match {
        case Left(configError) => Left(ParsingError.withMessages(configError.messages))
        case Right(None) => Right(opts)
        case Right(Some(devicesConfigs)) =>

          @tailrec
          def iter(
              acc: Either[Seq[String], List[Device]], devicesConfigs: List[Config]): Either[Seq[String], List[Device]] =
          {
            devicesConfigs match {
              case Nil => acc
              case deviceConfig :: xs =>
                val current = deviceConfig.extract[DeviceConfigModel] match {
                  case configs.Result.Success(deviceConfigModel) =>
                    extractDevice(deviceConfigModel, deviceConfig) match {
                      // don't append devices if there was error on previous steps
                      case Right(device) if acc.isRight =>
                        Right(acc.right.toOption.getOrElse(List.empty) :+ device)
                      // always replace previous success with errors
                      case Left(errors) =>
                        Left(acc.left.toOption.getOrElse(Seq.empty) ++: errors)
                      // skip new device if there was errors before
                      case _ => acc
                    }
                  case configs.Result.Failure(error) =>
                    Left(acc.left.toOption.getOrElse(Seq.empty) ++: error.messages)
                }
                iter(current, xs)
            }
          }

          iter(Right(List.empty), devicesConfigs) match {
            case Right(devices) =>
              val dublicates = devices.groupBy(_.id).collect { case (id, group) if group.length > 1 => id }
              if (dublicates.nonEmpty) {
                Left(ParsingError.withMessages(dublicates.map{id => s"more than one device with id '$id'"}.toSeq))
              } else {
                Right(devicesOpts(opts) { devicesOpts =>
                  val co2Devices = devices.collect { case device: Co2Device => device }
                  devicesOpts.copy(co2Devices = co2Devices)

                })
              }
            case Left(errors) => Left(ParsingError.withMessages(errors))
          }
      }
    }
    ()
  }

  def fillSelectedDeviceOptions(): Unit = {
    cliParser.opt[String]("device-id")
      .action { (value, opts) => opts.copy(selectedDeviceId = Some(value)) }
      .text { "Selected device" }

    ()
  }

  def co2DeviceRequired(): Unit = {
    appendCheck { opts =>
      val fromCliDefined = opts.fromCliDevice.isDefined
      val agentByIdDefined = opts.selectedDeviceId.isDefined &&
        !opts.selectedDeviceId.contains(Ambient7Options.CO2_FROM_CLI_ID)
      if (fromCliDefined && agentByIdDefined) {
        failure("Either --agent-name or --device-id should be defined, but not both")
      } else if (opts.universalConfigPath.isEmpty && opts.selectedDeviceId.isDefined) {
        failure("--device-id can be used only with --config")
      } else if (opts.selectedDeviceId.isDefined && opts.selectedCo2Device.isEmpty){
        val id = opts.selectedDeviceId.getOrElse("wtf?")
        failure(s"co2 device with id '$id' not found in config")
      } else {
        success
      }
    }
  }

  private def cliDeviceOpts(
      opts: Ambient7Options, deviceType: DeviceType.Type)(save: FromCliDevice => FromCliDevice): Ambient7Options =
  {

    val currentOpts = opts.fromCliDevice.getOrElse(FromCliDevice(deviceType = Some(deviceType)))
    opts.copy(
      fromCliDevice = Some(save(currentOpts))
    )
  }

  private def devicesOpts(opts: Ambient7Options)(save: Devices => Devices): Ambient7Options = {
    val currentDevices = opts.devices.getOrElse(Devices())
    opts.copy(
      devices = Some(save(currentDevices))
    )
  }

  // TODO: brake logic by device types, ex. separate object for every type
  private def extractDevice(
      deviceConfigModel: DeviceConfigModel, rawConfig: Config): Either[Seq[String], Device] =
  {
    // mt8057 the only known device type for now
    if (deviceConfigModel.model != "mt8057") {
      Left(List(s"unknown devices type '${deviceConfigModel.model}' for device with id = '${deviceConfigModel.id}'"))

    // tags defined, but format violated
    } else if (deviceConfigModel.agentTags.exists(AgentTags.tryParseFromString(_).isLeft)) {
      val tags = deviceConfigModel.agentTags.getOrElse("")
      Left(List(
        s"bad devices tags format '${tags}' for device with id = '${deviceConfigModel.id}'"
      ))

    } else {
      Right(Co2Device(
        deviceConfigModel.id,
        agent = Co2Agent(deviceConfigModel.agentName, AgentTags(deviceConfigModel.agentTags.getOrElse("")))
      ))
    }
  }

}
