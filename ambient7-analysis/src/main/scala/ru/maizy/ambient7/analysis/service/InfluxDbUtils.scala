package ru.maizy.ambient7.analysis.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success, Try }
import ru.maizy.ambient7.core.util.DateTimeIterator
import ru.maizy.influxdbclient.InfluxDbClient
import ru.maizy.influxdbclient.data.{ NumberValue, StringValue, Value }
import ru.maizy.influxdbclient.util.Escape.{ escapeIdentifier, escapeValue }
import ru.maizy.influxdbclient.util.Dates

private[service] object InfluxDbUtils {

  def getValuesForTimePeriod[T](
      client: InfluxDbClient,
      from: ZonedDateTime,
      until: ZonedDateTime,
      table: String,
      valueColumn: String,
      valueExtractor: Value => Option[T],
      aggregate: AggregateType,
      segment: Duration,
      timeColumn: String = "time",
      condition: String = "true"
    )(implicit ex: ExecutionContext): Future[List[(ZonedDateTime, Option[T])]] =
  {
    require(from.compareTo(until) < 0)
    require(segment.isFinite() && segment.toSeconds >= 1)

    val dateFrom = Dates.toInfluxDbFormat(from)
    val dateUntil = Dates.toInfluxDbFormat(until)

    val query =
      s"select ${aggregate(valueColumn)} as value " +
        s"from ${escapeIdentifier(table)} " +
        s"where ${escapeIdentifier(timeColumn)} >= ${escapeValue(dateFrom)} " +
        s"and ${escapeIdentifier(timeColumn)} < ${escapeValue(dateUntil)} " +
        s"and ($condition) " +
        s"group by time(${segment.toSeconds}s)"

    // TODO: little bit shitty code
    client.query(query) map { res =>
      val timeToValue: Map[ZonedDateTime, Option[T]] = res.firstSeriesItems.map { seriesItem =>
        (seriesItem.findColumnIndex(timeColumn), seriesItem.findColumnIndex("value")) match {
          case (Some(timeIndex), Some(valueIndex)) =>
            seriesItem.values.zipWithIndex.map { case (row, rowNum) =>
              val rawTime = row(timeIndex)
              rawTime match {
                case timeValue: StringValue =>
                  Dates.fromInfluxDbToZonedDateTime(timeValue.value) match {
                    case Success(time) =>
                      row(valueIndex) match {
                        case value: Value => (time, valueExtractor(value))
                        case _ => (time, None)
                      }

                    case Failure(e) => throw new Error(s"unparsed time $rawTime", e)
                  }
                case _ => throw new Error(s"time not found in row #$rowNum")
              }
            }.toMap
          case _ => throw new Error(s"time & value columns not found in results")
        }
      }.getOrElse(Map.empty)

      DateTimeIterator(from, until, segment).toList.map { time =>
        (time, timeToValue.getOrElse(time, None))
      }
    }
  }

  val intExtractor: (Value => Option[Int]) = {
    case value: NumberValue => Try(value.value.toInt) match {
      case Success(i) => Some(i)
      case Failure(_) => None
    }
    case _ => None
  }

  val floatExtractor: (Value => Option[Float]) = {
    case value: NumberValue => Try(value.value.toFloat) match {
      case Success(i) => Some(i)
      case Failure(_) => None
    }
    case _ => None
  }
}
