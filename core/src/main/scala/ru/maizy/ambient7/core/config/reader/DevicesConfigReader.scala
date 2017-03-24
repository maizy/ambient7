package ru.maizy.ambient7.core.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import com.typesafe.config.Config
import ru.maizy.ambient7.core.config.{ Ambient7Options, Defaults, FromCliDevice, ParsingError }
import ru.maizy.ambient7.core.data.{ AgentTags, Co2Agent, Co2Device, Device, DeviceType, Devices }
import ru.maizy.ambient7.core.notifications.{ AvailabilityWatcherSpec, Co2LevelChangedWatcherSpec, WatcherSpec }

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

  /**
   * TODO: see [[ru.maizy.ambient7.core.config.reader.NotificationsConfigReader.addActionsConfigRule]]
   */
  def fillDevicesOptions(parseWatchersConfig: Boolean = false): Unit = {
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
                    extractDevice(deviceConfigModel, deviceConfig, parseWatchersConfig) match {
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

    if (parseWatchersConfig) {
      appendWatchersChecks()
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
      deviceConfigModel: DeviceConfigModel, rawConfig: Config, parseWatchers: Boolean): Either[Seq[String], Device] =
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
      val eitherWatchers = if (parseWatchers) extractWatchers(rawConfig) else Right(List.empty[WatcherSpec])
      eitherWatchers.right.map { watchers =>
        Co2Device(
          deviceConfigModel.id,
          agent = Co2Agent(deviceConfigModel.agentName, AgentTags(deviceConfigModel.agentTags.getOrElse(""))),
          watchersSpecs = watchers
        )
      }
    }
  }

  /**
   * TODO: see [[ru.maizy.ambient7.core.config.reader.NotificationsConfigReader.addActionsConfigRule]]
   */
  private def extractWatchers(deviceConfig: Config): Either[Seq[String], List[WatcherSpec]] = {
    deviceConfig.get[Option[List[Config]]]("watchers").toEither match {
      case Left(configError) => Left(configError.messages)
      case Right(None) => Right(List.empty)
      case Right(Some(watchersConfigs)) =>

        @tailrec
        def iter(
            acc: Either[Seq[String], List[WatcherSpec]],
            watchersConfigs: List[Config]): Either[Seq[String], List[WatcherSpec]] =
        {
          watchersConfigs match {
            case Nil => acc
            case watcherConfig :: xs =>
              val current = extractWatcher(watcherConfig) match {
                case Right(action) if acc.isRight =>
                  Right(acc.right.toOption.getOrElse(List.empty) :+ action)
                case Left(errors) =>
                  Left(acc.left.toOption.getOrElse(Seq.empty) ++: errors)
                case _ => acc
              }
              iter(current, xs)
          }
        }

        iter(Right(List.empty), watchersConfigs)
    }
  }

  private def extractWatcher(watcherConfig: Config): Either[Seq[String], WatcherSpec] = {
    (watcherConfig.get[String]("type").toEither, watcherConfig.get[List[String]]("actions").toEither) match {
      case (Right(watcherType), Right(actionIds)) =>
        watcherType match {
          case "co2_level_changed" => buildCo2LevelChangedWatcherSpec(watcherConfig, actionIds)
          case "availability" => buildAvailabilityWatcherSpec(watcherConfig, actionIds)
          case _ => Left(List(s"unknown watcher type '$watcherType'"))
        }
      case _ => Left(List("type & action are required for action"))
    }
  }

  private def extractResolveAfterAndNotifyAfter(
      watcherConfig: Config): Either[Seq[String], (Option[Duration], Option[Duration])] =
  {
    watcherConfig.get[Option[Duration]]("notify_after") errorOrFlatmap { mayBeNotifyAfter =>
      watcherConfig.get[Option[Duration]]("resolve_after") errorOrFlatmap { mayBeResolveAfter =>
        Right((mayBeNotifyAfter, mayBeResolveAfter))
      }
    }
  }

  private def buildCo2LevelChangedWatcherSpec(
      watcherConfig: Config, actionIds: List[String]): Either[Seq[String], WatcherSpec] =
  {

    extractResolveAfterAndNotifyAfter(watcherConfig).right.flatMap { case (notifyAfter, resolveAfter) =>
      Right(Co2LevelChangedWatcherSpec(actionIds, notifyAfter, resolveAfter))
    }
  }

  private def buildAvailabilityWatcherSpec(
      watcherConfig: Config, actionIds: List[String]): Either[Seq[String], WatcherSpec] =
  {

    extractResolveAfterAndNotifyAfter(watcherConfig).right.flatMap { case (notifyAfter, resolveAfter) =>
      Right(AvailabilityWatcherSpec(actionIds, notifyAfter, resolveAfter))
    }
  }

  private def appendWatchersChecks(): Unit = {
    appendCheck { opts =>
      val availableActionsIds = opts.notifications
        .map { notifications =>
          notifications.actionsSpecs.map(_.id)
        }
        .getOrElse(List.empty)

      val errors = opts.devices
        .map(_.allDevices)
        .map {
          _.flatMap { device =>
            for (
              watcherSpec <- device.watchersSpecs;
              actionId <- watcherSpec.actionsIds
              if !availableActionsIds.contains(actionId)
            ) yield s"watcher '${watcherSpec.watcherType}' of device '${device.id}' contains unknown action $actionId"
          }
        }
        .getOrElse(List.empty)

      if (errors.isEmpty) {
        success
      } else {
        failure(errors.toIndexedSeq)
      }
    }
  }

}
