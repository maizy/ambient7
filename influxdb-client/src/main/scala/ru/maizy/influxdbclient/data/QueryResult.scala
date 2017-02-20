package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
case class QueryResult(results: IndexedSeq[Either[SeriesError, Series]]) {

  def headOption: Option[Series] = results.headOption.flatMap(_.right.toOption)

  def firstSeriesItems: Option[SeriesItem] = headOption.flatMap(_.headOption)

  def firstSeriesItemIfColumnNotEmpty(columnName: String): Option[SeriesItem] =
    firstSeriesItems
      .flatMap { seriesItems =>
        seriesItems.getColumnValues(columnName)
          .right.toOption
          // return seriesItems if values not empty
          .filterNot(_.isEmpty)
          .map(_ => seriesItems)
      }


  // TODO: combine with firstSeriesItemIfColumnNotEmpty ?
  def firstSeriesItemIfNumberColumnNotEmpty(columnName: String): Option[SeriesItem] =
    firstSeriesItems
      .flatMap { seriesItems =>
        seriesItems.getColumnNumberValues(columnName)
          .right.toOption
          .filterNot(_.isEmpty)
          .map(_ => seriesItems)
      }
}
