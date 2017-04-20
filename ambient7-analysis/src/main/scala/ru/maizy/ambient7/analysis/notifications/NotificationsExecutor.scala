package ru.maizy.ambient7.analysis.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.{ ZoneId, ZoneOffset, ZonedDateTime }
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.analysis.influxdb
import ru.maizy.ambient7.analysis.notifications.action.SlackAction
import ru.maizy.ambient7.analysis.notifications.data.{ Co2AgentData, Data }
import ru.maizy.ambient7.analysis.notifications.watcher.{ Co2LevelWatcher, Watcher }
import ru.maizy.ambient7.core.data.Device

class NotificationsExecutor(opts: Ambient7Options) {
  private case class WatchedDevice(device: Device, data: Data, watchers: List[Watcher])

  private val logger = notificationsLogger
  private var watchedDevices: List[WatchedDevice] = List.empty
  private var finishPromise: Option[Promise[Unit]] = None

  private implicit val notificationContext = ExecutionContext.global

  def start(): Future[Unit] = {
    val promise = Promise[Unit]()
    finishPromise = Some(promise)

    def onFailureProxyToPromise[T](future: Future[T]): Future[T] = {
      future onFailure { case error: Throwable => promise.failure(error) }
      future
    }

    onFailureProxyToPromise(init()) onSuccess { case _ =>
      onFailureProxyToPromise(sheduleDataUpdate())
    }

    promise.future
  }

  private def init(): Future[Unit] = {
    (opts.notifications, influxdb.buildClient(opts)) match {
      case (Some(notificationsOptions), Some(influxDbClient)) =>

        val refreshRate = notificationsOptions.refreshRate
        // TODO: init watchers
        // FIXME: tmp
        val maxRequestedDuration = 20.minutes
        val device = opts.co2Devices.head
        val co2Watcher = new Co2LevelWatcher()
        val action = new SlackAction
        co2Watcher.subscribe(action)

        watchedDevices =
          WatchedDevice(
            device,
            new Co2AgentData(device.agent, influxDbClient, refreshRate, maxRequestedDuration),
            // FIXME: tmp
            List(co2Watcher)
          ) :: watchedDevices
        Future.successful(())
      case _ => Future.failed(new Error("Some options required for notifications not provided"))
    }
  }

  // FIXME: add scheduler based on ScheduledExecutorService, current solution blocks one of EC thread
  private def sheduleDataUpdate(step: Long = 0): Future[Unit] = {
    opts.notifications.map(_.refreshRate) match {

      case Some(refreshRate) =>
        val start = ZonedDateTime.now()
        val nextUpdate = start.plusSeconds(refreshRate.toSeconds)

        logger.debug(s"Updating notification data, step #$step")
        val dataUpdateFuture = updateData()

        def checkAndScheduleNext(): Unit = {
          val end = ZonedDateTime.now()
          if (end.compareTo(nextUpdate) < 0) {
            val sleepTimeMillis = java.time.Duration.between(end, nextUpdate).toMillis
            logger.debug(s"Call next updating after ${sleepTimeMillis / 1000} seconds")
            Thread.sleep(sleepTimeMillis)
          } else {
            val updateTime = java.time.Duration.between(start, end).toMillis
            logger.warn(s"Update cycle (${updateTime / 1000}s) longer than refresh rate (${refreshRate.toSeconds}s)")
          }
          sheduleDataUpdate(step + 1)
          ()
        }

        dataUpdateFuture onComplete {
          case Success(_) =>
            // FIXME: tmp, remove $data
            logger.debug(s"Notification data has updated successful, step #$step")
            checkAndScheduleNext()

          case Failure(e) =>
            logger.error(s"Notification data hasn't updated, step #$step", e)
            checkAndScheduleNext()
        }

        Future.successful(())

        case _ => Future.failed(new Error("Requirements for date update isn't defined"))
    }
  }

  private def updateData(): Future[Unit] = {
    // FIXME: tmp
    val now = ZonedDateTime.of(2017, 3, 31, 16, 30, 13, 0, ZoneOffset.UTC)
      .withZoneSameInstant(ZoneId.systemDefault())

    val updateFutures = watchedDevices.map { device =>
      val future = device.data.update(now)
      future foreach { _ =>
        device.watchers.foreach(_.processData(device.data))
      }
      future
    }

    Future.sequence(updateFutures).map(_ => ())
  }
}
