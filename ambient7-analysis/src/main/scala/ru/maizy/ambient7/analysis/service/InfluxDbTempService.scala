package ru.maizy.ambient7.analysis.service

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.time.ZonedDateTime
import scala.concurrent.duration.{ Duration, DurationInt }
import scala.concurrent.{ ExecutionContext, Future }
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.ambient7.core.data.Co2Agent
import ru.maizy.influxdbclient.InfluxDbClient
import ru.maizy.influxdbclient.util.Escape.{ escapeValue, tagsToQueryCondition }

case class TempResult(celsius: Option[Float], from: ZonedDateTime, duration: Duration, aggregateType: AggregateType)

object InfluxDbTempService extends LazyLogging {

  def getValuesForTimePeriod(
      client: InfluxDbClient,
      agent: Co2Agent,
      from: ZonedDateTime,
      until: ZonedDateTime,
      segment: Duration = 10.seconds,
      aggregate: AggregateType = MEAN)(implicit ex: ExecutionContext): Future[List[TempResult]] =
  {

    InfluxDbUtils.getValuesForTimePeriod[Float](
      client,
      from,
      until,
      "temp",
      "celsius",
      InfluxDbUtils.floatExtractor,
      aggregate,
      segment,
      condition = s"agent = ${escapeValue(agent.agentName)} and ${tagsToQueryCondition(agent.tags.asPairs)}"
    ).map { results =>
      results.map { case (time, mayBeValue) =>
        TempResult(mayBeValue, time, segment, aggregate)
      }
    }
  }
}
