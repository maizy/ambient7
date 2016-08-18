package ru.maizy.influxdbclient.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

case class SeriesItem(name: String, columns: IndexedSeq[Column], values: IndexedSeq[IndexedSeq[Value]]) {

  def getColumnNumberValues(
      columnName: String,
      ignoreErrors: Boolean = false): Either[String, IndexedSeq[BigDecimal]] = {

    getColumnValues(columnName)
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

  /**
   * return column values, possible values are subclasses of ru.maizy.influxdbclient.data.Value.
   * if column not found, returns Left (if ignoreErrors == false) or ignore it (overwise)
   */
  def getColumnValues(columnName: String, ignoreErrors: Boolean = false): Either[String, IndexedSeq[Value]] = {
    val q = '"'
    findColumnIndex(columnName) match {
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

  def findColumn(name: String): Option[Column] = columns.find(_.name == name)

  def findColumnIndex(name: String): Option[Int] = columns.zipWithIndex.find(_._1.name == name).map(_._2)
}
