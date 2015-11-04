package ru.maizy.ambient7

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 *
 * Based on
 * * https://github.com/dmage/co2mon/blob/01eb85aa806e8bd67fcf6c8f5107fbf499ff3b32/libco2mon/src/co2mon.c#L91-L118
 * * https://github.com/Lokis92/h1/blob/a292d85cf35b07e2ae945d49be2b1dfb904b0fb8/co2java/src/Co2mon.java#L183-L202
 */

import scala.util.{ Success, Failure, Try }


sealed trait ResultValue
case class Temp(celsus: Double) extends ResultValue
case class Co2(ppm: Int, high: Boolean = false) extends ResultValue


class ParseError(message: String) extends Exception(message)
class UnknownDataCode(message: String) extends ParseError(message)


object MessageDecoder {

  private val MAGIC_WORD = Array(0x48, 0x74, 0x65, 0x6d, 0x70, 0x39, 0x39, 0x65).map(_.toByte)  // Htemp99e
  private val MAGIC_WORD_SHIFTED = MAGIC_WORD.map(i => i << 4 | i >> 4).map(_.toByte)
  private val CO2_CODE = 0x50.toByte
  private val TEMP_CODE = 0x42.toByte
  private val ABS_ZERO_TEMP = 273.15
  private val TEMP_FACTOR = 0.0625
  private val EXPECTED_CO2_BOUND = 0 to 3000

  def decode(rawData: Array[Byte]): Try[Array[Byte]] = {
    Try {
      require(rawData.length == 8, "rawData should have length of 8 bytes")
      var res = rawData.clone()
      swap(res)
      res = shift(res)
      decodeWithMagicWord(res)
      res
    }
  }

  /**
   * swap in place
   */
  private[ambient7] def swap(data: Array[Byte]): Unit = {
    def byteSwap(index1: Int, index2: Int): Unit =  {
      val tmp = data(index2)
      data(index2) = data(index1)
      data(index1) = tmp
    }
    byteSwap(0, 2)
    byteSwap(1, 4)
    byteSwap(3, 7)
    byteSwap(5, 6)
  }

  /**
   * shift
   */
  private[ambient7] def shift(data: Array[Byte]): Array[Byte] = {

    def byteShift(index1: Int, index2: Int): Byte =
      (((data(index1) << 5).toByte & 0xe0) | ((data(index2) >> 3).toByte & 0x1f)).toByte

    val res = new Array[Byte](8)
    res(7) = byteShift(6, 7)
    res(6) = byteShift(5, 6)
    res(5) = byteShift(4, 5)
    res(4) = byteShift(3, 4)
    res(3) = byteShift(2, 3)
    res(2) = byteShift(1, 2)
    res(1) = byteShift(0, 1)
    res(0) = byteShift(7, 0)
    res
  }

  /**
   * decode in place
   */
  private[ambient7] def decodeWithMagicWord(data: Array[Byte]): Unit = {
    for (i <- data.indices) {
      data(i) = (data(i) - MAGIC_WORD_SHIFTED(i)).toByte
    }
  }

  def checkCRC(data: Array[Byte]): Boolean = {
    if (data.length < 5) {
      false
    } else {
      data(4) == 0x0d.toByte && (data(0) + data(1) + data(2)).toByte == data(3)
    }
  }

  def parseValue(data: Array[Byte]): Try[ResultValue] = {

    def bytesToLong(b1: Byte, b2: Byte): Long = {
      val r1 = (b1 + 256) % 256
      val r2 = (b2 + 256) % 256
      (r1 << 8) + r2
    }

    if (data.length < 5) {
      Failure(new IllegalArgumentException(s"wrong data length ${data.length} but should be >= 5"))
    } else {
      data match {
        case Array(CO2_CODE, b1, b2, _*) =>
          val ppm = bytesToLong(b1, b2).toInt
          // according to ZG01C spec expected value in range of 0..3000
          // device may send higher values, but them not precise
          if (ppm < EXPECTED_CO2_BOUND.min) {
            Failure(new ParseError(s"co2 value not in expected $EXPECTED_CO2_BOUND"))
          } else if (ppm > EXPECTED_CO2_BOUND.max) {
            Success(Co2(EXPECTED_CO2_BOUND.max, high = true))
          } else {
            Success(Co2(ppm))
          }
        case Array(TEMP_CODE, b1, b2, _*) => Success(Temp(decodeTemperature(bytesToLong(b1, b2))))
        case Array(u, _*) => Failure(new ParseError(s"unknown data code $u"))
      }
    }
  }

  private def decodeTemperature(value: Long): Double = value.toDouble * TEMP_FACTOR - ABS_ZERO_TEMP


}
