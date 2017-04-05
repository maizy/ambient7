package ru.maizy.ambient7.analysis.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.temporal.ChronoUnit
import java.time.ZonedDateTime
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import ru.maizy.ambient7.analysis.service.{ Co2LevelResult, InfluxDbCo2Service, InfluxDbTempService, TempResult }
import ru.maizy.ambient7.core.data.Co2Agent
import ru.maizy.ambient7.core.util.{ DateTimeIterator, Dates }
import ru.maizy.influxdbclient.InfluxDbClient

class Co2Data(
    val agent: Co2Agent,
    influxDbClient: InfluxDbClient,
    val refreshRate: Duration,
    val storeDuration: Duration)(implicit private val ec: ExecutionContext)
  extends Data
{

  private val segment = 5.seconds min refreshRate
  private val limit = Co2Data.computeLimit(segment, storeDuration)
  val co2 = new DataPoints[Option[Int]](limit)
  val temp = new DataPoints[Option[Float]](limit)
  val availability = new DataPoints[Boolean](limit)

  def update(now: ZonedDateTime = ZonedDateTime.now()): Future[Unit] = {

    val nowTruncated = Dates.truncateDateTime(now, segment)
    val from = nowTruncated.minus(storeDuration.toMillis, ChronoUnit.MILLIS)

    def updateDataPoints(co2Results: List[Co2LevelResult], tempResult: List[TempResult]): Unit = {
      co2Results.foreach { level =>
        co2.appendPoint(level.from, level.ppm)
      }

      tempResult.foreach { value =>
        temp.appendPoint(value.from, value.celsius)
      }
      val co2NonEmptyPoints = co2Results.collect {case Co2LevelResult(Some(_), time, _, _) => time}
      val tempNonEmptyPoints = tempResult.collect {case TempResult(Some(_), time, _, _) => time}

      for(time <- DateTimeIterator(from, now, segment)) {
        availability.appendPoint(time, co2NonEmptyPoints.contains(time) || tempNonEmptyPoints.contains(time))
      }
    }

    val co2Future = InfluxDbCo2Service.getValuesForTimePeriod(
      influxDbClient,
      agent,
      from,
      now,
      segment = segment
    )

    val tempFuture = InfluxDbTempService.getValuesForTimePeriod(
      influxDbClient,
      agent,
      from,
      now,
      segment = segment
    )

    for(
      co2Res <- co2Future;
      tempRes <- tempFuture
    ) yield updateDataPoints(co2Res, tempRes)

  }


  override def toString: String =
    s"Co2Data(limit=$limit, agent=${agent.agentName}, refreshRate=$refreshRate, storeDuration=$storeDuration,\n" +
      s"co2=$co2,\ntemp=$temp,\navailability=$availability\n)"
}

object Co2Data {
  def computeLimit(refreshRate: Duration, storeDuration: Duration): Int =
    (storeDuration.toSeconds.toDouble / refreshRate.toSeconds).floor.toInt + 1
}
