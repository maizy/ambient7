package ru.maizy.ambient7.analysis.notifications

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration.DurationInt
import scala.util.Success
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.analysis.influxdb
import ru.maizy.ambient7.analysis.notifications.data.Data

class NotificationsExecutor(opts: Ambient7Options) {
  private val logger = notificationsLogger
  var data: Option[Data] = None
  val finishPromise: Option[Future[Unit]] = None

  private implicit val notificationContext = ExecutionContext.global

  def start(): Future[Unit] = {
    val finishPromise = Promise[Unit]()

    def onFailureProxyToPromise[T](future: Future[T]): Future[T] = {
      future onFailure { case error: Throwable => finishPromise.failure(error) }
      future
    }

    onFailureProxyToPromise(init()) onSuccess { case _ =>
      onFailureProxyToPromise(sheduleDataUpdate())
    }

    finishPromise.future
  }

  private def init(): Future[Unit] = {
    (opts.notifications, influxdb.buildClient(opts)) match {
      case (Some(notificationsOptions), Some(influxDbClient)) =>

        val refreshRate = notificationsOptions.refreshRate
        // TODO: init watchers
        // FIXME: tmp
        val maxRequestedDuration = 20.minutes

        data = Some(Data(influxDbClient, refreshRate, maxRequestedDuration))
        Future.successful(())
      case _ => Future.failed(new Error("Some options required for notifications not provided"))
    }
  }

  // FIXME: add scheduler based on ScheduledExecutorService, current solution blocks one of EC thread
  private def sheduleDataUpdate(step: Long = 0): Future[Unit] = {
    (opts.notifications.map(_.refreshRate), data) match {

      case (Some(refreshRate), Some(dataContainer)) =>
        val start = ZonedDateTime.now()
        val nextUpdate = start.plusSeconds(refreshRate.toSeconds)

        logger.debug(s"Updating notification data, step #$step")
        val dataUpdateFuture = dataUpdate(dataContainer)

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
            logger.debug(s"Notification data has updated successful, step #$step: $data")
            checkAndScheduleNext()

          case util.Failure(e) =>
            logger.error(s"Notification data hasn't updated, step #$step", e)
            checkAndScheduleNext()
        }

        Future.successful(())

        case _ => Future.failed(new Error("Requirements for date update not defined"))
    }
  }

  private def dataUpdate(dataContainer: Data): Future[Unit] = dataContainer.update()
}
