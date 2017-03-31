package ru.maizy.ambient7.analysis.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.Duration
import ru.maizy.influxdbclient.InfluxDbClient

class Data private (influxDbClient: InfluxDbClient, val limit: Int) {

  val co2 = new DataPoints[Option[Int]](limit)
  val temp = new DataPoints[Option[Float]](limit)
  val availability = new DataPoints[Boolean](limit)

  def update(): Future[Unit] = {
    implicit val ec = ExecutionContext.Implicits.global
    influxDbClient.query("select ppm from co2 limit 10").map { res =>
      println(res)
      ()
    }
  }
}

object Data {
  def apply(influxDbClient: InfluxDbClient, refreshRate: Duration, storeDuration: Duration): Data =
    new Data(influxDbClient, computeLimit(refreshRate, storeDuration))

  def computeLimit(refreshRate: Duration, storeDuration: Duration): Int =
    (storeDuration.toSeconds.toDouble / refreshRate.toSeconds).floor.toInt + 1
}
