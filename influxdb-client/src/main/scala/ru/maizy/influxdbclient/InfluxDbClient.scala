package ru.maizy.influxdbclient

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.net.URLEncoder
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration.{ DurationInt, FiniteDuration }
import scala.collection.JavaConverters.{ collectionAsScalaIterableConverter, mapAsScalaMapConverter }
import scala.util.{ Failure, Success, Try }
import scalaj.http.{ BaseHttp, HttpOptions, HttpResponse }
import com.typesafe.scalalogging.LazyLogging
import dispatch.{ Http, Req, as, implyRequestHandlerTuple, url }
import ru.maizy.influxdbclient.data.{ ErrorDto, QueryResult }
import ru.maizy.influxdbclient.responses.QueryResultsProtocol._
import spray.json.{ DeserializationException, JsonParser, ParserInput }

// TODO: remove sync methods (iss #39)
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


  def rawDataQuery(query: String, readOnly: Boolean = true)(implicit ec: ExecutionContext): Future[RawHttpResponse] = {
    val settings = if (readOnly) influxDbReadonlySettings else influxDbSettings
    logger.debug(s"${settings.user}@${settings.db}: $query")
    val request = buildBaseRequest("query", settings)
      .addQueryParameter("db", settings.db)
      .addQueryParameter("q", query)

    Http(request > as.Response { response =>
      val headers = response.getHeaders.asScala.mapValues(_.asScala.toIndexedSeq).toMap
      RawHttpResponse(response.getStatusCode, response.getResponseBodyAsBytes, headers)
    })
  }

  def query(query: String, readOnly: Boolean = true)(implicit ec: ExecutionContext): Future[QueryResult] = {
    rawDataQuery(query, readOnly) map { raw =>
      parseQueryResult(raw) match {
        case Right(r) => r
        case Left(error) => throw new Error(error.message, error.cause)
      }

    }
  }

  // TODO: remove ErrorDto use Error instead
  private def parseQueryResult(response: RawHttpResponse): Either[ErrorDto, QueryResult] = {
    if (response.code != 200) {
      Left(parserErrorResponse(response))
    } else {
      parserQueryResponse(response)
    }
  }

  @deprecated("use async methods", "0.4")
  def syncRawDataQuery(query: String): Either[ErrorDto, HttpResponse[Array[Byte]]] = {
    logger.debug(s"db: ${influxDbReadonlySettings.db}, query: $query")
    val request = buildBaseSyncRequest("query", influxDbReadonlySettings)
      .param("db", influxDbReadonlySettings.db)
      .param("q", query)

    Try(request.asBytes) match {
      case Failure(e) => Left(ErrorDto("Unable to perform request", Some(e)))
      case Success(response) => Right(response)
    }
  }

  @deprecated("use async methods", "0.4")
  def syncQuery(query: String): Either[ErrorDto, QueryResult] = {
    syncRawDataQuery(query).right.flatMap { response =>
      parseQueryResult(RawHttpResponse(response.code, response.body, response.headers))
    }
  }

  // TODO: remove ErrorDto use Error instead
  private def parserErrorResponse(response: RawHttpResponse): ErrorDto =
    Try(JsonParser(ParserInput(response.body)).convertTo[ErrorDto]) match {
      case Success(error) => error
      case Failure(e: DeserializationException) =>
        ErrorDto(s"response deserialization error (response code ${response.code})", Some(e))
      case Failure(e: Throwable) =>
        ErrorDto(s"unknown response deserialization error (response code ${response.code})", Some(e))
    }

  // TODO: remove ErrorDto use Error instead
  private def parserQueryResponse(response: RawHttpResponse): Either[ErrorDto, QueryResult] =
    Try(JsonParser(ParserInput(response.body)).convertTo[QueryResult]) match {
      case Success(result) =>
        Right(result)
      case Failure(e: DeserializationException) =>
        Left(ErrorDto("response deserialization error", Some(e)))
      case Failure(e: Throwable) =>
        Left(ErrorDto("unknown response deserialization error", Some(e)))
    }

  private def buildBaseSyncRequest(method: String, settings: InfluxDbConnectionSettings) = {
    val request = httpClient.apply(buildUri(method, settings))
    (settings.user, settings.password) match {
      case (Some(user), Some(password)) => request.auth(user, password)
      case _ => request
    }
  }

  private def buildBaseRequest(method: String, settings: InfluxDbConnectionSettings): Req  = {
    val request = url(buildUri(method, settings))
    (settings.user, settings.password) match {
      case (Some(user), Some(password)) => request.as(user, password)
      case _ => request
    }
  }

  // TODO: use dispatch.url constructor instead of a string
  private def buildUri(method: String, settings: InfluxDbConnectionSettings): String =
    settings.baseUrl.stripSuffix("/") + "/" + urlencode(method)

  private def urlencode(value: String) = URLEncoder.encode(value, "UTF-8")
}
