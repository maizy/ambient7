package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

// TODO: separate artefact

import java.net.URLEncoder
import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import scalaj.http.{ BaseHttp, HttpOptions }

class InfluxDbClient(
    val influxDbSettings: InfluxDbConnectionSettings,
    val userAgent: Option[String] = None,
    val connectTimeout: FiniteDuration = 200.millis,
    val readTimeout: FiniteDuration = 2000.millis,
    private val _influxDbReadonlySettings: Option[InfluxDbConnectionSettings] = None
  ) {

  require(connectTimeout.toMillis < Int.MaxValue)
  require(readTimeout.toMillis < Int.MaxValue)

  val influxDbReadonlySettings: InfluxDbConnectionSettings = _influxDbReadonlySettings.getOrElse(influxDbSettings)

  private val httpClient: BaseHttp =
    new BaseHttp (
      userAgent = userAgent.getOrElse("ambient7"),
      options = Seq(
        HttpOptions.connTimeout(connectTimeout.toMillis.toInt),
        HttpOptions.readTimeout(readTimeout.toMillis.toInt),
        HttpOptions.followRedirects(false)
      )
    )

  def rawDataQuery(query: String): Array[Byte] = {
    val request = buildBaseRequest("query", influxDbReadonlySettings)
      .param("db", influxDbReadonlySettings.db)
      .param("q", query)

    request.asBytes.body
  }

  private def buildBaseRequest(method: String, settings: InfluxDbConnectionSettings) = {
    val request = httpClient.apply(buildUri(method, settings))
    (settings.user, settings.password) match {
      case (Some(user), Some(password)) => request.auth(user, password)
      case _ => request
    }
  }

  private def buildUri(method: String, settings: InfluxDbConnectionSettings): String =
    settings.baseUrl.stripSuffix("/") + "/" + urlencode(method)

  private def urlencode(value: String) = URLEncoder.encode(value, "UTF-8")
}
