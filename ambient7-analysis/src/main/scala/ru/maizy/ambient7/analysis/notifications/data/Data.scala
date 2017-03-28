package ru.maizy.ambient7.analysis.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.Duration
import ru.maizy.influxdbclient.InfluxDbClient

class Data private (influxDbClient: InfluxDbClient, val limit: Int) {

  val co2 = new DataPoints[Option[Int]](limit)
  val temp = new DataPoints[Option[Float]](limit)
  val availability = new DataPoints[Boolean](limit)

  def update(): Either[Seq[String], Unit] = {
    // FIXME
    Left(Seq("todo"))
  }
}

object Data {
  def apply(influxDbClient: InfluxDbClient, refreshRate: Duration, storeDuration: Duration): Data =
    new Data(influxDbClient, computeLimit(refreshRate, storeDuration))

  def computeLimit(refreshRate: Duration, storeDuration: Duration): Int =
    (storeDuration.toSeconds.toDouble / refreshRate.toSeconds).floor.toInt + 1
}
