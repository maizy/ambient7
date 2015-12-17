package ru.maizy.ambient7.mt8057agent

import scala.collection.mutable
import scalaj.http.{ BaseHttp, HttpRequest }

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class InfluxDbWriter(opts: AppOptions) extends Writer {
  import InfluxDbWriter._

  override def write(event: Event): Unit = {
    formatLine(event).foreach { data =>
      buildWriteRequest(data).asString
      // if (responce.code != 204) {  // TODO: logging
      //
      // }
    }
  }

  override def onInit(): Unit = {}

  private def formatLine(event: Event): Option[String] = {
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
    userAgent = "ambient7/" + AppOptions.APP_VERSION
  )
}
