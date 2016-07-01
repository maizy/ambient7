package ru.maizy.influxdbclient.dto

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
case class SeriesItem(name: String, columns: IndexedSeq[Column], values: IndexedSeq[IndexedSeq[Value]])
