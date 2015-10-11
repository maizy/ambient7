package ru.maizy.ambient7agent

import javax.usb.event.{ UsbPipeErrorEvent, UsbPipeDataEvent, UsbPipeListener }

import ru.maizy.ambient7agent.utils.Debug
import ru.maizy.ambient7agent.utils.Debug.prettyByteArray

import scala.collection.JavaConversions
import javax.usb._

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */
object AgentApp extends App {

  val VENDOR_ID = 0x04d9.toShort
  val PRODUCT_ID = 0xa052.toShort


  // FIXME: dirty prototype
  def findDevice(hub: UsbHub, vendorId: Short, productId: Short): Option[UsbDevice] = {
    val devicesJavaList: java.util.List[UsbDevice] = hub.getAttachedUsbDevices.asInstanceOf[java.util.List[UsbDevice]]
    val devices = JavaConversions.asScalaBuffer(devicesJavaList)

    for (device <- devices) {
      val desc = device.getUsbDeviceDescriptor
      if (desc.idVendor == VENDOR_ID && desc.idProduct == PRODUCT_ID) {
        return Some(device)
      }

      if (device.isUsbHub) {
        val maybeRes = findDevice(device.asInstanceOf[UsbHub], vendorId, productId)
        if (maybeRes.isDefined) {
          return maybeRes
        }
      }
    }
    None
  }

  val services: UsbServices = UsbHostManager.getUsbServices
  val device = findDevice(services.getRootUsbHub, VENDOR_ID, PRODUCT_ID)
  device match {
    case Some(d) => {
      println(s"Device found: $d")
      val configuration = d.getActiveUsbConfiguration
      println(s"config string: ${configuration.getConfigurationString}")
      val interface = configuration.getUsbInterfaces.get(0).asInstanceOf[UsbInterface]

      val irp: UsbControlIrp = d.createUsbControlIrp(
        (UsbConst.REQUESTTYPE_DIRECTION_IN | UsbConst.REQUESTTYPE_TYPE_STANDARD | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE).toByte,
        UsbConst.REQUEST_GET_CONFIGURATION,
        0.toShort,
        0.toShort
      )
      irp.setData(Array[Byte](1))
      d.syncSubmit(irp)
      println("control irp:")
      println(prettyByteArray(irp.getData))

      interface.claim()
      try {
        val endpoints = interface.getUsbEndpoints.asInstanceOf[java.util.List[UsbEndpoint]]
        println(s"interface: $interface has ${endpoints.size} endpoints, get first")
        val endpoint: UsbEndpoint = endpoints.get(0)
        println(s"endpoint: $endpoint (${endpoint.getType})")
        val pipe: UsbPipe = endpoint.getUsbPipe
        println(s"pipe: $pipe")
//        pipe.addUsbPipeListener(
//          new UsbPipeListener {
//
//            override def errorEventOccurred(event: UsbPipeErrorEvent): Unit = {
//              val error: UsbException = event.getUsbException
//              println(s"shit happens $error")
//            }
//
//            override def dataEventOccurred(event: UsbPipeDataEvent): Unit = {
//              val data = event.getData
//              println("some data")
//              println(prettyByteArray(data))
//            }
//          }
//        )
        try {
          pipe.open()
          var data = new Array[Byte](1)
          val received: Int = pipe.syncSubmit(data)
          println(s"$received bytes received: $data");
        } finally {
          pipe.close()
        }

      } finally {
        interface.release()
      }
    }
    case None => println(s"Device not found")
  }
}
