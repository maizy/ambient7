package ru.maizy.ambient7.core.config.reader

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import com.typesafe.config.Config
import ru.maizy.ambient7.core.config.options.NotificationsOptions
import ru.maizy.ambient7.core.config.{ Ambient7Options, Defaults, ParsingError }
import ru.maizy.ambient7.core.notifications.{ ActionSpec, ActionTemplates, InMemoryActionSpec, SlackActionSpec }
import ru.maizy.ambient7.core.notifications.StdoutActionSpec


trait NotificationsConfigReader extends UniversalConfigReader {

  import UniversalConfigReader._
  import configs.syntax._

  implicit class ErrorOrFlatmap[T, V](result: configs.Result[T]) {
    def errorOrFlatmap(map: T => Either[Seq[String], V]): Either[Seq[String], V] = {
      result.toEither match {
        case Right(r) => map(r)
        case Left(errors) => Left(errors.messages)
      }
    }
  }

  private def notificationsOpts(
      opts: Ambient7Options)(save: NotificationsOptions => NotificationsOptions): Ambient7Options =
    opts.copy(notifications = Some(save(opts.notifications.getOrElse(NotificationsOptions()))))

  def fillNotificationsOptions(): Unit = {

    cliParser.opt[Duration]("refresh-rate")
      .valueName { s"<${Defaults.NOTIFICATION_REFRESH_RATE.toMinutes}m>" }
      .action { (value, opts) => notificationsOpts(opts)(_.copy(refreshRate = value)) }

    appendSimpleOptionalConfigRule[Duration]("notifications.refresh_rate") { (value, opts) =>
      notificationsOpts(opts)(_.copy(refreshRate = value))
    }

    appendCheck { opts =>
      if (opts.notifications.exists(o => !o.refreshRate.isFinite() || o.refreshRate.toSeconds <= 0)) {
        failure("refresh rate should be finite & great than 0s")
      } else {
        success
      }
    }

    addActionsConfigRule()

    ()
  }

  // TODO: generalize copy-paste from ru.maizy.ambient7.core.config.reader.DevicesConfigReader#fillDevicesOptions
  private def addActionsConfigRule(): Unit = {

    appendConfigRule { (config, opts) =>
      config.get[Option[List[Config]]]("actions").toEither match {
        case Left(configError) => Left(ParsingError.withMessages(configError.messages))
        case Right(None) => Right(opts)
        case Right(Some(actionsConfig)) =>

          @tailrec
          def iter(
              acc: Either[Seq[String], Seq[ActionSpec]],
              actionsConfig: List[Config]): Either[Seq[String], Seq[ActionSpec]] =
          {
            actionsConfig match {
              case Nil => acc
              case actionConfig :: xs =>
                val current = extractAction(actionConfig) match {
                  // don't append action if there was error on previous steps
                  case Right(action) if acc.isRight =>
                    Right(acc.right.toOption.getOrElse(Seq.empty) :+ action)
                  // always replace previous success with errors
                  case Left(errors) =>
                    Left(acc.left.toOption.getOrElse(Seq.empty) ++: errors)
                  // skip new action if there was errors before
                  case _ => acc
                }
                iter(current, xs)
            }
          }

          iter(Right(List.empty), actionsConfig) match {
            case Right(actions) =>
              val dublicates = actions.groupBy(_.id).collect { case (id, group) if group.length > 1 => id }
              if (dublicates.nonEmpty) {
                Left(ParsingError.withMessages(dublicates.map{id => s"more than one action with id '$id'"}.toSeq))
              } else {
                Right(notificationsOpts(opts)(_.copy(actionsSpecs = actions.toList)))
              }
            case Left(errors) => Left(ParsingError.withMessages(errors))
          }
      }
    }

    ()
  }

  private def extractAction(actionConfig: Config): Either[Seq[String], ActionSpec] = {

    (actionConfig.get[String]("type").toEither, actionConfig.get[String]("id").toEither) match {
      case (Right(actionTypeCode), Right(actionId)) =>
        actionTypeCode match {
          case "slack" => buildSlackActionSpec(actionId, actionConfig)
          case "stdout" => buildStdoutActionSpec(actionId, actionConfig)
          case "in_memory" => buildInMemoryActionSpec(actionId, actionConfig)
          case _ => Left(List(s"unknown action type '$actionTypeCode'"))
        }
      case _ => Left(List(s"type & id are required for action"))
    }
  }

  private def buildSlackActionSpec(actionId: String, actionConfig: Config): Either[Seq[String], ActionSpec] = {
    withTemplates(actionConfig) { templates =>
      // TODO: better syntax needed
      actionConfig.get[String]("url") errorOrFlatmap { url =>
        actionConfig.get[Option[String]]("channel") errorOrFlatmap { maybeChannel =>
          actionConfig.get[Option[String]]("icon") errorOrFlatmap { maybeIcon =>
            Right(SlackActionSpec(
              actionId,
              url = url,
              templates = templates,
              channel = maybeChannel,
              icon = maybeIcon
            ))
          }
        }
      }
    }
  }

  private def buildStdoutActionSpec(actionId: String, actionConfig: Config): Either[Seq[String], ActionSpec] = {
    withTemplates(actionConfig) { templates =>
      Right(StdoutActionSpec(actionId, templates))
    }
  }

  private def buildInMemoryActionSpec(actionId: String, actionConfig: Config): Either[Seq[String], ActionSpec] = {
    val limit = actionConfig.get[Int]("limit").toOption.getOrElse(InMemoryActionSpec.DEFAULT_LIMIT)
    Right(InMemoryActionSpec(actionId, limit))
  }

  private def withTemplates(
      actionConfig: Config)(f: ActionTemplates => Either[Seq[String], ActionSpec]): Either[Seq[String], ActionSpec] = {
    val eitherTemplates =
      actionConfig.get[Option[String]]("templates.co2_level_changed_increased") errorOrFlatmap { co2increased =>
        actionConfig.get[Option[String]]("templates.co2_level_changed_decreased") errorOrFlatmap { co2decreased =>
          actionConfig.get[Option[String]]("templates.unavailable") errorOrFlatmap { unavailable =>
            actionConfig.get[Option[String]]("templates.available") errorOrFlatmap { available =>
              Right(ActionTemplates(
                co2increased,
                co2decreased,
                available,
                unavailable
              ))
            }
          }
        }
      }

    eitherTemplates.right.flatMap(f)
  }


}
