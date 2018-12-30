#include "Arduino.h"
#include <SoftwareSerial.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include "configs.h"
#include "Ambient7Wifi.h"
#include "Ambient7Co2.h"


unsigned long startTime = millis();

SoftwareSerial co2Serial(CO2_RX, CO2_TX);


void setup()
{
  Serial.begin(115200);
  Serial.println("");
  Serial.println("Setup: ");
  Serial.print("CO2 RX Pin: "); Serial.println(CO2_RX);
  Serial.print("CO2 TX Pin: "); Serial.println(CO2_TX);
  Serial.print("Wifi network: "); Serial.println(WIFI_NETWORK);
  Serial.print("Wifi password: ");
  for (unsigned int i = 0; i < strlen(WIFI_PASSWORD); i++) {
    Serial.print('*');
  }
  Serial.println("");

  setupWifi(WIFI_NETWORK, WIFI_PASSWORD);
}

void loop()
{
  Serial.print("Time from start: ");
  Serial.print((millis() - startTime) / 1000);
  Serial.println(" s");

  int co2Ppm = readCO2UART(co2Serial, 1000);
  Serial.print("Co2 result: "); Serial.println(co2Ppm);

  delay(5000);
}
