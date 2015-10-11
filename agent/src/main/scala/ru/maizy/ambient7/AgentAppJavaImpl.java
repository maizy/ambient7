package ru.maizy.ambient7;

import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.NANOSECONDS;


public class AgentAppJavaImpl implements HidServicesListener { // FIXME: port to scala

  static final short VENDOR_ID = 0x04d9;
  static final short PRODUCT_ID = (short) 0xa052;  // -41042 (10)
  static final byte[] MAGIC_WORD = {0x48, 0x74, 0x65, 0x6d, 0x70, 0x39, 0x39, 0x65};  // Htemp99e

  private HidServices hidServices;

  public void executeExample() throws HidException {

    System.out.println("Loading hidapi...");

    // Get HID services
    hidServices = HidManager.getHidServices();
    hidServices.addHidServicesListener(this);

    System.out.println("Enumerating attached devices...");

    // Provide a list of attached devices
    hidServices.getAttachedHidDevices().forEach(System.out::println);

    HidDevice device = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null);
    if (device != null) {
      System.out.println("ever attached");
      sendInitialise(device);
    } else {
      System.out.println("Waiting for device attach...");
    }
    // Stop the main thread to demonstrate attach and detach events
    sleepUninterruptibly(5, TimeUnit.SECONDS);

    if (device != null && device.isOpen()) {
      device.close();
    }

    System.exit(0);
  }

  @Override
  public void hidDeviceAttached(HidServicesEvent event) {

    System.out.println("Device attached: " + event);

    if (event.getHidDevice().getVendorId() == VENDOR_ID && event.getHidDevice().getProductId() == PRODUCT_ID) {
      HidDevice device = hidServices.getHidDevice(VENDOR_ID, PRODUCT_ID, null);
      if (device != null) {
        sendInitialise(device);
      }

    }

  }

  @Override
  public void hidDeviceDetached(HidServicesEvent event) {

    System.err.println("Device detached: " + event);

  }

  @Override
  public void hidFailure(HidServicesEvent event) {

    System.err.println("HID failure: " + event);

  }

  private void sendInitialise(HidDevice device) {

    System.out.println("Work with device " + device);

    System.out.println("Send future report");
    byte[] magicTable = new byte[8];
    byte reportId = 0x0;
    int resFuture = device.sendFeatureReport(magicTable, reportId);
    System.out.println("resFuture report " + resFuture);

//    // Send the Initialise message
//    byte[] message = new byte[64];
//    message[0] = 0x0;
//
//    int val = device.write(message, PACKET_LENGTH, (byte) 0);
//    if (val != -1) {
//      System.out.println("> [" + val + "]");
//    } else {
//      System.err.println(device.getLastErrorMessage());
//    }

    System.out.println("Try read data");
    // Prepare to read a single data packet
    for (int step = 0; step <= 64; step++) {
      //System.out.println("step " + step);

      byte data[] = new byte[8];
      // This method will now block for 5000ms or until data is read
      int value = device.read(data, 5000);
      switch (value) {
        case -1:
          System.err.println(device.getLastErrorMessage());
          break;
        case 0:
          break;
        default:
          System.out.print("< [");
          for (byte b : data) {
            System.out.printf("%02x ", b);
          }
          System.out.println("]");
//          System.out.print("] = ");
//          byte[] decoded = decodeBuffer(data, magicTable);
//          for (byte b : decoded) {
//            System.out.printf(" %02x", b);
//          }
//          System.out.println();
          break;
      }
    }
  }

//  private void swapBytes(byte[] data, int index1, int index2) {
//    final byte tmp = data[index1];
//    data[index1] = data[index2];
//    data[index1] = tmp;
//  }

//  private byte[] decodeBuffer(byte[] rawData, byte[] magicTable) throws IllegalArgumentException {
//
//    if (rawData.length != 8) {
//      throw new IllegalArgumentException("rawData should have length of 8 bytes");
//    }
//
//    byte[] tmpRes = new byte[rawData.length];
//    System.arraycopy(rawData, 0, tmpRes, 0, rawData.length);
//
//    swapBytes(tmpRes, 0, 2);
//    swapBytes(tmpRes, 1, 4);
//    swapBytes(tmpRes, 3, 7);
//    swapBytes(tmpRes, 5, 6);
//
//    for (int i = 0; i < 8; ++i) {
//        tmpRes[i] ^= magicTable[i];
//    }
//
//    byte[] result = new byte[rawData.length];
//    byte tmp = (byte) (tmpRes[7] << 5);
//    result[7] = (byte) ((tmpRes[6] << 5) | (tmpRes[7] >> 3));
//    result[6] = (byte) ((tmpRes[5] << 5) | (tmpRes[6] >> 3));
//    result[5] = (byte) ((tmpRes[4] << 5) | (tmpRes[5] >> 3));
//    result[4] = (byte) ((tmpRes[3] << 5) | (tmpRes[4] >> 3));
//    result[3] = (byte) ((tmpRes[2] << 5) | (tmpRes[3] >> 3));
//    result[2] = (byte) ((tmpRes[1] << 5) | (tmpRes[2] >> 3));
//    result[1] = (byte) ((tmpRes[0] << 5) | (tmpRes[1] >> 3));
//    result[0] = (byte) (tmp | (tmpRes[0] >> 3));
//
//    for (int i = 0; i < 8; ++i) {
//        result[i] -= (MAGIC_WORD[i] << 4) | (MAGIC_WORD[i] >> 4);
//    }
//
//    return result;
//  }

  /**
   * Invokes {@code unit.}{@link java.util.concurrent.TimeUnit#sleep(long) sleep(sleepFor)}
   * uninterruptibly.
   */
  public static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
    boolean interrupted = false;
    try {
      long remainingNanos = unit.toNanos(sleepFor);
      long end = System.nanoTime() + remainingNanos;
      while (true) {
        try {
          // TimeUnit.sleep() treats negative timeouts just like zero.
          NANOSECONDS.sleep(remainingNanos);
          return;
        } catch (InterruptedException e) {
          interrupted = true;
          remainingNanos = end - System.nanoTime();
        }
      }
    } finally {
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    }
  }

}
