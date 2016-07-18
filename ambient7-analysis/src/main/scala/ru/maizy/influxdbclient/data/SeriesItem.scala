package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class SeriesItem(name: String, columns: IndexedSeq[Column], values: IndexedSeq[IndexedSeq[Value]]) {

  def getNumberColumn(columnName: String, ignoreErrors: Boolean = false): Either[String, IndexedSeq[BigDecimal]] = {
    getColumn(columnName)
      .right.flatMap { values =>
        val nonNullValue = values.filterNot(_ == NullValue)
        val foundBigDecimals = nonNullValue
          .collect {
            case num: NumberValue => num.value
          }

        if (!ignoreErrors && foundBigDecimals.size != nonNullValue.size) {
          val q = '"'
          Left(s"Some rows doesn't contain number column $q$columnName$q")
        } else {
          Right(foundBigDecimals)
        }
      }
    }

  def getColumn(columnName: String, ignoreErrors: Boolean = false): Either[String, IndexedSeq[Value]] = {
    val mayBeColumnIndex = columns.zipWithIndex.find(_._1.name == columnName).map(_._2)
    val q = '"'
    mayBeColumnIndex match {
      case None => Left(s"Column $q$columnName$q not found")
      case Some(index) =>
        val found = values.collect{ case row: IndexedSeq[Value] if row.size > index => row(index) }
        if (!ignoreErrors && found.size != values.size) {
          Left(s"Some rows doesn't contain column $q$columnName$q with index $index")
        } else {
          Right(found)
        }
    }
  }
}
