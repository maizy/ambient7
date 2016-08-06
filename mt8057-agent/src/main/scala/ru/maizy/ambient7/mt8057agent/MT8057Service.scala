package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import java.util.{Queue => JavaQueue}
import scala.util.{ Success, Failure }
import scala.collection.JavaConversions.asScalaBuffer
import com.typesafe.scalalogging.LazyLogging
import org.hid4java.{ HidServices, HidManager, HidDevice, HidServicesListener }
import org.hid4java.event.HidServicesEvent

// TODO send usb errors as separate events type
class MT8057Service(
    private val queue: JavaQueue[Event],
    private val hidServices: HidServices)
  extends HidServicesListener with LazyLogging {

  private val VENDOR_ID = 0x04d9
  private val PRODUCT_ID = 0xa052.toShort

  private object States extends Enumeration {
    type State = Value
    val Unknown, Wait, Init, Read = Value
  }

  import States._
  private var state: States.State = States.Unknown
  private var device: Option[HidDevice] = None

  def currentNanoTime(): Long = System.currentTimeMillis * 1000000

  def run(): Unit = {
    state = Wait
    deviceLoop()
  }

  private def deviceLoop(): Unit = {
    device = findDevice()
    if (device.isDefined) {
      queue.add(DeviceUp(currentNanoTime()))
    }
    val standardDelay = 500L
    var delay = standardDelay  // ms
    while (true) {
      state match {

        case Unknown =>
          closeDevice()
          state = Wait
          delay = standardDelay * 2

        case Wait =>
          if (device.isEmpty) {
            device = findDevice()
          }
          device match {
            case Some(d) =>
              state = Init
              delay = 50L
            case None =>
              delay = standardDelay * 2
          }

        case Init =>
          if (device.isDefined && initDevice()) {
            state = Read
            delay = 50L
          } else {
            logger.warn("init error")
            closeDevice()
            state = Wait
            delay = standardDelay * 2
          }

        case Read =>
          if (device.isEmpty || !readData()) {
            logger.warn("read error")
            closeDevice()
            state = Wait
            delay = standardDelay * 2
          } else {
            delay = standardDelay
          }
      }
      Thread.sleep(delay)
    }
  }

  private def findDevice(): Option[HidDevice] = {
    val matched = asScalaBuffer(hidServices.getAttachedHidDevices).filter(isMatchedDevice).toSeq
    if (matched.size > 1) {
      logger.error("More than one matched USB HID devices")
      None
    } else if (matched.isEmpty) {
      None
    } else {
      // hid4java works only if get device like this
      // device from getAttachedHidDevices doesn't work properly
      Option(hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID.toInt, null)) // scalastyle:ignore
    }
  }

  private def isMatchedDevice(device: HidDevice): Boolean =
    Option(device).exists(d => d.getVendorId == VENDOR_ID && d.getProductId == PRODUCT_ID)

  private def initDevice(): Boolean = {
    device.exists {
      val packageSize = 8
      val magicTable = Array.ofDim[Byte](packageSize) // send zero table
      _.sendFeatureReport(magicTable, 0x0.toByte) == packageSize + 1
    }
  }

  private def closeDevice(): Unit = {
    this.synchronized {
      device.filter(_.isOpen).foreach(_.close)
      device = None
    }
  }

  private def readData(): Boolean = {
    val data = Array.ofDim[Byte](8)
    device.exists {
      _.read(data, 5000) match {
        case -1 =>  // unknown error
          false
        case 0 =>  // no data to read
          true
        case _ =>
          MessageDecoder.decode(data) match {
            case Failure(e) =>
              logger.warn(s"Decode problem", e)
            case Success(decoded) =>
              MessageDecoder.checkCRC(decoded) match {
                case false =>
                  logger.warn("Bad CRC")
                case true =>
                  MessageDecoder.parseValue(decoded).foreach {
                    case v: Co2 => queue.add(Co2Updated(v, currentNanoTime()))
                    case v: Temp => queue.add(TempUpdated(v, currentNanoTime()))
                  }
              }
          }
          true
      }
    }
  }

  // HidServicesListener methods called from hid4java thread

  override def hidDeviceDetached(event: HidServicesEvent): Unit = {
    if (isMatchedDevice(event.getHidDevice)) {
      queue.add(DeviceDown(currentNanoTime()))
      closeDevice()
    }
  }

  override def hidDeviceAttached(event: HidServicesEvent): Unit = {
    if (isMatchedDevice(event.getHidDevice)) {
      // just fire event, device loop will find device on next step
      queue.add(DeviceUp(currentNanoTime()))
    }
  }

  override def hidFailure(event: HidServicesEvent): Unit = {
    val message = Option(event.getHidDevice).map(_.getLastErrorMessage)
    logger.error("HidError: " + message.getOrElse("<unknown>"))
  }
}


object MT8057Service {
  def run(queue: JavaQueue[Event]): Unit = {
    val hidServices = HidManager.getHidServices
    val service = new MT8057Service(queue, hidServices)
    hidServices.addHidServicesListener(service)
    service.run()
  }
}
