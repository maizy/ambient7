package ru.maizy.ambient7.mt8057agent.tests

import org.scalactic.Tolerance
import org.scalatest.TryValues
import ru.maizy.ambient7.mt8057agent.{ MessageDecoder, Co2, Temp }

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015-2017
 * See LICENSE.txt for details.
 */
class MessageDecoderSpec
  extends AbstractBaseSpec
  with TryValues
  with Tolerance {

  trait TestData {
    val raw: Array[Byte] =         Array(0xbd, 0xa4, 0x32, 0xb6, 0xce, 0x9a, 0x9c, 0x58).map(_.toByte)
    val afterSwap: Array[Byte] =   Array(0x32, 0xce, 0xbd, 0x58, 0xa4, 0x9c, 0x9a, 0xb6).map(_.toByte)
    val afterShift: Array[Byte] =  Array(0xc6, 0x59, 0xd7, 0xab, 0x14, 0x93, 0x93, 0x56).map(_.toByte)
    val result: Array[Byte] =      Array(0x42, 0x12, 0x81, 0xd5, 0x0d, 0x00, 0x00, 0x00).map(_.toByte)

    // TODO: more samples, bad co2 samples (>3000)
    val tempSamples = Seq[(Array[Byte], Double)](
      (raw, 22.9125)
    )
    val co2Samples = Seq[(Array[Byte], Int)](
      (Array(0x7e, 0xa4, 0xa2, 0xb6, 0x4f, 0x9a, 0x9c, 0x08).map(_.toByte), 665)
    )

  }

  "MessageDecoder.decode" should "fails if data isn't 8 bytes length" in {
    (MessageDecoder.decode(Array(0xbd, 0xa4, 0x32, 0xb6).map(_.toByte)).failure.exception
      shouldBe a[IllegalArgumentException])
  }

  it should "swap bytes as a first step" in {
    new TestData {
      val data = raw.clone()
      MessageDecoder.swap(data)
      data shouldBe afterSwap
    }
  }

  it should "shift bytes as a second step" in {
    new TestData {
      MessageDecoder.shift(afterSwap) shouldBe afterShift
    }
  }

  it should "apply magic word as a third step" in {
    new TestData {
      val data = afterShift.clone()
      MessageDecoder.decodeWithMagicWord(data)
      data shouldBe result
    }
  }

  it should "do all work together" in {
    new TestData {
      MessageDecoder.decode(raw).success.value shouldBe result
    }
  }

  "MessageDecoder.checkCRC" should "check only array length >= 4" in {
    MessageDecoder.checkCRC(new Array[Byte](3)) shouldBe false
  }

  it should "return true on positive data" in {
    new TestData {
      MessageDecoder.checkCRC(result) shouldBe true
    }
  }

  it should "return false on negative data" in {
    new TestData {
      for (i <- 0 to 3) {
        val data = result.clone()
        data(i) = 0xFF.toByte
        MessageDecoder.checkCRC(data) shouldBe false
      }
    }
  }

  "MessageDecoder.parseValue" should "decode positive data with temp" in {
    new TestData {
      for ((sample, expected) <- tempSamples) {
        val decoded = MessageDecoder.decode(sample).success.value
        MessageDecoder.checkCRC(decoded) shouldBe true
        MessageDecoder.parseValue(decoded).success.value match {
          case v: Temp => v.celsius shouldBe (expected +- 0.1)
          case _ => throw new AssertionError("not expected value")
        }
      }
    }
  }

  it should "decode positive data with co2" in {
    new TestData {
      for ((sample, expected) <- co2Samples) {
        val decoded = MessageDecoder.decode(sample).success.value
        MessageDecoder.checkCRC(decoded) shouldBe true
        MessageDecoder.parseValue(decoded).success.value match {
          case v: Co2 => v.ppm shouldBe expected
          case _ => throw new AssertionError("not expected value")
        }
      }
    }
  }
}
