package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case class QueryResult(results: IndexedSeq[Either[SeriesError, Series]]) {
  def headOption: Option[Series] = results.headOption.flatMap(_.right.toOption)
}
