#include "Arduino.h"
#include <SoftwareSerial.h>
#include "configs.h"


unsigned long startTime = millis();

SoftwareSerial co2Serial(CO2_RX, CO2_TX);

byte getCheckSum(byte *packet)
{
  byte i;
  byte checksum = 0;
  for (i = 1; i < 8; i++) {
    checksum += packet[i];
  }
  checksum = 0xff - checksum;
  checksum += 1;
  return checksum;
}

int readCO2UART()
{
  byte cmd[9] = {0xFF,0x01,0x86,0x00,0x00,0x00,0x00,0x00,0x79};
  byte response[9]; // for answer

  Serial.println("Sending CO2 request...");
  co2Serial.write(cmd, 9); //request PPM CO2

  // clear the buffer
  memset(response, 0, 9);

  int i = 0;
  while (co2Serial.available() == 0) {
    delay(1000);
    i++;
  }

  if (co2Serial.available() > 0) {
      co2Serial.readBytes(response, 9);
  }

  // print out the response in hex
  for (int i = 0; i < 9; i++) {
    Serial.print(String(response[i], HEX));
    Serial.print("   ");
  }
  Serial.println("");

  // checksum
  byte check = getCheckSum(response);
  if (response[8] != check) {
    Serial.println("Checksum not OK!");
    Serial.print("Received: ");
    Serial.println(response[8]);
    Serial.print("Should be: ");
    Serial.println(check);
  }

  // ppm
  int ppm_uart = 256 * (int)response[2] + response[3];
  Serial.print("PPM UART: ");
  Serial.println(ppm_uart);

  // temp
  byte temp = response[4] - 40;
  Serial.print("Temperature? ");
  Serial.println(temp);

  // status
  byte status = response[5];
  Serial.print("Status? ");
  Serial.println(status);
  if (status == 0x40) {
    Serial.println("Status OK");
  }

  return ppm_uart;
}

void setup()
{
  Serial.begin(115200);
  Serial.println("");
  Serial.println("Setup: ");
  Serial.print("CO2 RX Pin: "); Serial.println(CO2_RX);
  Serial.print("CO2 TX Pin: "); Serial.println(CO2_TX);
  Serial.print("Wifi network: "); Serial.println(WIFI_NETWORK);
}

void loop()
{
  Serial.print("Time from start: ");
  Serial.print((millis() - startTime) / 1000);
  Serial.println(" s");

  int ppm_uart = readCO2UART();

  delay(5000);
}
