package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */

import scala.util.{ Failure, Success, Try }
import scalaj.http.{ BaseHttp, HttpOptions, HttpRequest, HttpResponse }
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.core.config.options.InfluxDbOptions
import ru.maizy.ambient7.core.data.{ AgentTag, AgentTags }

class InfluxDbWriter(opts: Ambient7Options) extends Writer with LazyLogging {
  import InfluxDbWriter._
  val OK_NO_CONTENT = 204

  override def write(event: Event): Unit = {
    formatLine(event).foreach { data =>
      opts.influxDb match {
        case None => logger.error(s"Unable to perform influxdb request: options not available")
        case Some(influxDbOptions) =>
          val request = buildWriteRequest(data, influxDbOptions)
          val responseRes = performRequest(request)
          // TODO: buffer for N events if a failure happens (iss #14)
          responseRes match {
            case Failure(e) =>
              logger.warn(s"Unable to perform influxdb request: ${e.getClass}")
              logger.debug(s"Request error", e)
            case Success(response) if response.code != OK_NO_CONTENT =>
              logger.warn(s"Unable to write event to influxdb: HTTP ${response.code} ${response.body}")
            case _ =>
          }
      }
    }
  }

  override def onInit(): Unit = {}

  private[mt8057agent] def formatLine(event: Event): Option[String] = {
    event match {
      case Co2Updated(Co2(co2, high), ts) =>
        Some(s"co2$tags ppm=${co2}i,high=$high $ts")
      case TempUpdated(Temp(temp), ts) =>
        val formattedTemp = temp.formatted("%.2f").replace(",", ".")
        Some(s"temp$tags celsius=$formattedTemp $ts")
      case _ => None
    }
  }

  private lazy val tags: String = {
    opts.selectedCo2Device match {
      case Some(device) =>
        val finalTags = AgentTags(
          device.agent.tags.tags ++ IndexedSeq(
            AgentTag("agent", device.agent.agentName),
            AgentTag("device", "mt8057")
          )
        )
        "," + finalTags.encoded
      case _ => ""
    }
  }

  private[mt8057agent] def performRequest(request: HttpRequest): Try[HttpResponse[String]] =
    Try(request.asString)

  private def buildWriteRequest(data: String, influxDbOpts: InfluxDbOptions): HttpRequest = {
    // TODO: migrate to influxdb client
    val baseUrl = influxDbOpts.baseUrl.stripSuffix("/")
    var request = HttpClient(s"$baseUrl/write")
      .postData(data)

    request = influxDbOpts.database match {
      case Some(dbName) => request.param("db", dbName)
      case _ => request
    }

    request = (influxDbOpts.user, influxDbOpts.password) match {
      case (Some(user), Some(pass)) =>
        request.auth(user, pass)
      case _ =>
        request
    }

    request
  }
}


object InfluxDbWriter {
  object HttpClient extends BaseHttp (
      userAgent = "ambient7", // TODO: app version
      options = Seq(
        HttpOptions.connTimeout(200),
        HttpOptions.readTimeout(200),
        HttpOptions.followRedirects(false)
      )
    )
}
