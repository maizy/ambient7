package ru.maizy.influxdbclient.data

import ru.maizy.influxdbclient.BaseSpec

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */
class SeriesItemSpec extends BaseSpec {
  val sample = SeriesItem(
    "some",
    columns = IndexedSeq(Column("label"), Column("val")),
    values = IndexedSeq(
      IndexedSeq(StringValue("red"), NumberValue(1)),
      IndexedSeq(StringValue("green"), NumberValue(2)),
      IndexedSeq(StringValue("blue"), NumberValue(3))
    )
  )

  "SeriesItem" should "return values column" in {
    sample.getColumn("label") shouldBe Right(IndexedSeq(
      StringValue("red"), StringValue("green"), StringValue("blue")
    ))
  }

  it should "return number column" in {
    sample.getNumberColumn("val") shouldBe Right(IndexedSeq(
      BigDecimal(1), BigDecimal(2), BigDecimal(3)
    ))
  }

  it should "filter null values in number column" in {
    val withNull = SeriesItem(
      "some",
      columns = IndexedSeq(Column("val")),
      values = IndexedSeq(
        IndexedSeq(NullValue),
        IndexedSeq(NumberValue(3)),
        IndexedSeq(NumberValue(2)),
        IndexedSeq(NullValue),
        IndexedSeq(NumberValue(1))
      )
    )
    withNull.getNumberColumn("val") shouldBe Right(IndexedSeq(
      BigDecimal(3), BigDecimal(2), BigDecimal(1)
    ))
  }
}
