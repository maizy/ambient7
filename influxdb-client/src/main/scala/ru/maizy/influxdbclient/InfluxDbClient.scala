package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import java.net.URLEncoder
import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import scala.util.{ Failure, Success, Try }
import scalaj.http.{ BaseHttp, HttpOptions, HttpResponse }
import com.typesafe.scalalogging.LazyLogging
import ru.maizy.influxdbclient.data.{ ErrorDto, QueryResult }
import ru.maizy.influxdbclient.responses.QueryResultsProtocol._
import spray.json.{ DeserializationException, JsonParser, ParserInput }

class InfluxDbClient(
    val influxDbSettings: InfluxDbConnectionSettings,
    val userAgent: Option[String] = None,
    val connectTimeout: FiniteDuration = 200.millis,
    val readTimeout: FiniteDuration = 2000.millis,
    private val _influxDbReadonlySettings: Option[InfluxDbConnectionSettings] = None
  ) extends LazyLogging {

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

  def rawDataQuery(query: String): Either[ErrorDto, HttpResponse[Array[Byte]]] = {
    logger.debug(s"db: ${influxDbReadonlySettings.db}, query: $query")
    val request = buildBaseRequest("query", influxDbReadonlySettings)
      .param("db", influxDbReadonlySettings.db)
      .param("q", query)

    Try(request.asBytes) match {
      case Failure(e) => Left(ErrorDto("Unable to perform request", Some(e)))
      case Success(response) => Right(response)
    }
  }

  def query(query: String): Either[ErrorDto, QueryResult] = {
    rawDataQuery(query).right.flatMap { response =>
      if (response.code != 200) {
        Left(parserErrorResponse(response))
      } else {
        parserQueryResponse(response)
      }
    }
  }

  private def parserErrorResponse(response: HttpResponse[Array[Byte]]): ErrorDto =
    Try(JsonParser(ParserInput(response.body)).convertTo[ErrorDto]) match {
      case Success(error) => error
      case Failure(e: DeserializationException) =>
        ErrorDto(s"response deserialization error (response code ${response.code})", Some(e))
      case Failure(e: Throwable) =>
        ErrorDto(s"unknown response deserialization error (response code ${response.code})", Some(e))
    }

  private def parserQueryResponse(response: HttpResponse[Array[Byte]]): Either[ErrorDto, QueryResult] =
    Try(JsonParser(ParserInput(response.body)).convertTo[QueryResult]) match {
      case Success(result) =>
        Right(result)
      case Failure(e: DeserializationException) =>
        Left(ErrorDto("response deserialization error", Some(e)))
      case Failure(e: Throwable) =>
        Left(ErrorDto("unknown response deserialization error", Some(e)))
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
