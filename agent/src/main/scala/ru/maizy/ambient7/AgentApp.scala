package ru.maizy.ambient7

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import java.util.concurrent.TimeUnit
import org.hid4java.{ HidServices, HidServicesListener, HidManager, HidDevice }
import org.hid4java.event.HidServicesEvent
import scala.util.{ Success, Failure }

// FIXME: ATTENTION! this is very dirty prototype. It has many gotchas with threads & crappy code.
// FIXME: currently doesn't work with waitingLoop
// scalastyle:off
object AgentApp extends App with HidServicesListener {

  val VENDOR_ID = 0x04d9
  val PRODUCT_ID = 0xa052.toShort
  val MAGIC_WORD = Array(0x48, 0x74, 0x65, 0x6d, 0x70, 0x39, 0x39, 0x65)

  private var hidServices: HidServices = _
  private var attached = false
  println("Loading hidapi...")
  hidServices = HidManager.getHidServices
  hidServices.addHidServicesListener(this)
  println("Enumerating attached devices...")
  val device = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null)
  if (device != null) {
    attached = true
    println("ever attached")
    deviceLoop(device)
  } else {
    waitingLoop()
  }
  if (device != null && device.isOpen) {
    device.close()
  }
  System.exit(0)

  // FIXME: jvm crashed, need threads syncronization
  def waitingLoop(): Unit = {
    println("Waiting for device attach... at " + Thread.currentThread().getName)
    while(!attached) {
      sleepUninterruptibly(5, TimeUnit.SECONDS)
      println(".")
    }
  }

  override def hidDeviceAttached(event: HidServicesEvent) {
    println("attached at " + Thread.currentThread().getName)
    println("Device attached: " + event)
    if (event.getHidDevice.getVendorId == VENDOR_ID && event.getHidDevice.getProductId == PRODUCT_ID) {
      val device = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null)
      if (device != null) {
        attached = true
        println("wait 20 seconds before work with device")
        for (_ <- 0 to 20) {
          print(".")
          sleepUninterruptibly(1, TimeUnit.SECONDS)
        }
        deviceLoop(device)
      }
    }
  }

  override def hidDeviceDetached(event: HidServicesEvent) {
    println("detached at " + Thread.currentThread().getName)
    println("Device detached: " + event)
    if (event.getHidDevice.getVendorId == VENDOR_ID && event.getHidDevice.getProductId == PRODUCT_ID) {
      attached = false
      waitingLoop()
    }
  }

  override def hidFailure(event: HidServicesEvent) {
    println("HID failure: " + event)
  }

  private def deviceLoop(device: HidDevice) {
    println("Work with device " + device)
    println("Send future report")
    val magicTable = new Array[Byte](8)
    val reportId = 0x0.toByte
    val resFuture = device.sendFeatureReport(magicTable, reportId)
    println("resFuture report " + resFuture)
    println("Try read data")
    var continue = true
    while (continue) {
      val data = Array.ofDim[Byte](8)
      val value = device.read(data, 5000)
      value match {
        case -1 => println(s"device error: ${device.getLastErrorMessage}")
          continue = false
        case 0 => //break
        case _ =>
          MessageDecoder.decode(data) match {
            case Failure(e) => println(s"Decode problem $e")
            case Success(decoded) =>
              MessageDecoder.checkCRC(decoded) match {
                case false => println("bad CRC")
                case true =>
                  MessageDecoder.parseValue(decoded).foreach {
                    case Co2(ppm, _) => println(s"co2: ${ppm}ppm")
                    case Temp(celsus) => println(f"temp: $celsus%4.2f˚C")
                  }
              }
          }
      }
    }
    println("out of device loop")
  }

  def sleepUninterruptibly(sleepFor: Long, unit: TimeUnit) {
    var interrupted = false
    try {
      var remainingNanos = unit.toNanos(sleepFor)
      val end = System.nanoTime() + remainingNanos
      while (true) {
        try {
          TimeUnit.NANOSECONDS.sleep(remainingNanos)
          return
        } catch {
          case e: InterruptedException => {
            interrupted = true
            remainingNanos = end - System.nanoTime()
          }
        }
      }
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt()
      }
    }
  }
}
// scalastyle:on
