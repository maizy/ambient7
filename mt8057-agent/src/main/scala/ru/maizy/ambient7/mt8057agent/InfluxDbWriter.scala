package ru.maizy.ambient7.mt8057agent

import scala.collection.mutable
import scala.util.{ Success, Failure, Try }
import scalaj.http.{ HttpOptions, HttpResponse, BaseHttp, HttpRequest }
import com.typesafe.scalalogging.LazyLogging

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class InfluxDbWriter(opts: AppOptions) extends Writer with LazyLogging {
  import InfluxDbWriter._
  val OK_NO_CONTENT = 204

  override def write(event: Event): Unit = {
    formatLine(event).foreach { data =>
      val request = buildWriteRequest(data)
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
    val tags = mutable.ListBuffer[(String, String)]()
    opts.influxDbAgentName foreach { n =>
      tags += (("agent", n))
    }
    tags += (("device", "mt8057"))
    val additionalTags = opts.influxDbTags
    if (additionalTags.length > 0) {
      additionalTags.split("""(?<!\\),""")
        .foreach { pair =>
          val parts = pair.split("=")
          if (parts.length == 2) {
            tags += ((parts(0), parts(1)))
          }
        }
    }
    "," + tags
      .sortWith { case (p1, p2) => p1._1.compareTo(p2._1) < 0 }
      .map {case (key, value) => s"$key=$value"}
      .mkString(",")
  }

  private[mt8057agent] def performRequest(request: HttpRequest): Try[HttpResponse[String]] =
    Try(request.asString)

  private def buildWriteRequest(data: String): HttpRequest = {
    var request = HttpClient(opts.influxDbBaseUrl)
      .postData(data)

    request = opts.influxDbDatabase match {
      case Some(dbName) => request.param("db", dbName)
      case _ => request
    }

    request = (opts.influxDbUser, opts.influxDbPassword) match {
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
    userAgent = "ambient7/" + AppOptions.APP_VERSION,
    options = Seq(
      HttpOptions.connTimeout(200),
      HttpOptions.readTimeout(200),
      HttpOptions.followRedirects(false)
    )
  )
}
