#include "Arduino.h"
#include <SoftwareSerial.h>
//#include <ESP8266WebServer.h>
//#include <ESP8266mDNS.h>
#include "configs.h"
#include "Ambient7Wifi.h"
#include "Ambient7Co2.h"
#include "DHT.h"


unsigned long startTime = millis();

SoftwareSerial co2Serial(CO2_RX, CO2_TX);
DHT dht;

void setup()
{
  Serial.begin(115200);
  Serial.println(""); Serial.println("");
  Serial.println("Ambient7");
  Serial.println("https://github.com/maizy/ambient7/");
  Serial.println("");
  Serial.println("Configs: ");
  Serial.print("  CO2 RX Pin: "); Serial.println(CO2_RX);
  Serial.print("  CO2 TX Pin: "); Serial.println(CO2_TX);
  Serial.print("  DHT22 1wire Pin: "); Serial.println(DTH22_1WIRE);
  Serial.print("  Wifi network: "); Serial.println(WIFI_NETWORK);
  Serial.print("  Wifi password: ");
  for (unsigned int i = 0; i < strlen(WIFI_PASSWORD); i++) {
    Serial.print('*');
  }
  Serial.println("");
  Serial.println("");

  Serial.print("Setup DHT22: ...");
  dht.setup(DTH22_1WIRE);
  Serial.println(" done");

  setupWifi(WIFI_NETWORK, WIFI_PASSWORD);
  Serial.println("");
}

void loop()
{
  Serial.print("Uptime: "); Serial.print((millis() - startTime) / 1000); Serial.println(" s");

  int co2Ppm = readCO2UART(co2Serial, 1000);
  if (co2Ppm < 0) {
    Serial.println("ERROR: failed to read from Co2 sensor");
  } else {
    Serial.print("Co2: "); Serial.print(co2Ppm); Serial.println("PPM");
  }

  // TODO: to separete thread 
  float humidity = dht.getHumidity();
  float tempCelcius = dht.getTemperature();
  if (isnan(humidity) || isnan(tempCelcius)) {
    Serial.println("ERROR: failed to read from DHT sensor");
  } else {
    Serial.print("Humidity: "); Serial.print(humidity); Serial.println(" %");
    Serial.print("Temp: "); Serial.print(tempCelcius); Serial.println(" ˚C");
  }

  delay(5000);
  Serial.println("");
}
