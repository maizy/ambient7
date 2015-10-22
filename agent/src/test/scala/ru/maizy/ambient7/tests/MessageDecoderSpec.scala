package ru.maizy.ambient7.tests

import org.scalatest.TryValues
import ru.maizy.ambient7.MessageDecoder

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
class MessageDecoderSpec extends AbstractBaseSpec with TryValues {

  trait TempSample {

    //                                      0     1     2     3     4     5     6     7
    val raw: Array[Byte] =         Array(0xbd, 0xa4, 0x32, 0xb6, 0xce, 0x9a, 0x9c, 0x58).map(_.toByte)
    val afterSwap: Array[Byte] =   Array(0x32, 0xce, 0xbd, 0x58, 0xa4, 0x9c, 0x9a, 0xb6).map(_.toByte)
    val afterShift: Array[Byte] = Array(0xc6, 0x59, 0xd7, 0xab, 0x14, 0x93, 0x93, 0x56).map(_.toByte)

    val result: Array[Byte] = Array(0x42, 0x12, 0x81, 0xd5, 0x0d, 0x00, 0x00, 0x00).map(_.toByte)

    val expectedTemp = 22.9125
  }

  "MessageDecoder.decode" should "fails if data isn't 8 bytes length" in {
    (MessageDecoder.decode(Array(0xbd, 0xa4, 0x32, 0xb6).map(_.toByte)).failure.exception
      shouldBe a[IllegalArgumentException])
  }

  it should "swap bytes as a first step" in {
    new TempSample {
      val data = raw.clone()
      MessageDecoder.swap(data)
      data shouldBe afterSwap
    }
  }

  it should "shift bytes as a second step" in {
    new TempSample {
      MessageDecoder.shift(afterSwap) shouldBe afterShift
    }
  }

  it should "apply magic word as a third step" in {
    new TempSample {
      val data = afterShift.clone()
      MessageDecoder.decodeWithMagicWord(data)
      data shouldBe result
    }
  }

  it should "do all work together" in {
    new TempSample {
      MessageDecoder.decode(raw).success.value shouldBe result
    }
  }

  "MessageDecoder.checkCRC" should "check only array length >= 4" in {
    MessageDecoder.checkCRC(new Array[Byte](3)) shouldBe false
  }

  it should "return true on positive data" in {
    new TempSample {
      MessageDecoder.checkCRC(result) shouldBe true
    }
  }

  it should "return false on negative data" in {
    new TempSample {
      for (i <- 0 to 3) {
        val data = result.clone()
        data(i) = 0xFF.toByte
        MessageDecoder.checkCRC(data) shouldBe false
      }
    }
  }
}
