package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */
case class Series(series: IndexedSeq[SeriesItem]) {
  def headOption: Option[SeriesItem] = series.headOption
}
