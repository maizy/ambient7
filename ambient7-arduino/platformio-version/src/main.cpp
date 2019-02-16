#include "Arduino.h"
#include <SoftwareSerial.h>
#include <ESP8266WiFi.h>
#include "configs.h"
#ifdef ONLINE_MODE
  //#include <ESP8266WebServer.h>
  //#include <ESP8266mDNS.h>
  #include "Ambient7Wifi.h"
#endif
#include <DHT.h>
#include <MHZ19_uart.h>


unsigned long startTime = millis();

MHZ19_uart mhz19;
DHT dht;

void setup()
{
  Serial.begin(9600);
  Serial.println(""); Serial.println("");
  Serial.println("Ambient7");
  Serial.println("https://github.com/maizy/ambient7/");
  Serial.println("");
  Serial.println("Configs: ");
  Serial.print("  CO2 RX Pin: "); Serial.println(CO2_RX);
  Serial.print("  CO2 TX Pin: "); Serial.println(CO2_TX);
  Serial.print("  DHT22 1wire Pin: "); Serial.println(DTH22_1WIRE);
  #ifdef ONLINE_MODE
    Serial.print("  Wifi network: "); Serial.println(WIFI_NETWORK);
    Serial.print("  Wifi password: ");
    for (unsigned int i = 0; i < strlen(WIFI_PASSWORD); i++) {
      Serial.print('*');
    }
  #else
    Serial.println("  Offline mode");
  #endif
  Serial.println("");
  Serial.println("");

  Serial.print("Setup DHT22: ...");
  dht.setup(DTH22_1WIRE);
  Serial.println(" done");

  Serial.println("Setup MH-Z19 ...");
  mhz19.begin(CO2_RX, CO2_TX);
  mhz19.setAutoCalibration(false);
  Serial.println("Done");

  #ifdef ONLINE_MODE
    setupWifi(WIFI_NETWORK, WIFI_PASSWORD);
    Serial.println("");
  #else
    WiFi.mode(WIFI_OFF);
  #endif

  Serial.println("");
  Serial.println("");
}

void loop()
{
  Serial.print("DATA: uptime="); Serial.print((millis() - startTime) / 1000); Serial.println("s");
  int mhZ19Status = mhz19.getStatus();
  Serial.print("DATA: mh_z19_status="); Serial.println(mhZ19Status);
  int co2Ppm = mhz19.getPPM();
  if (co2Ppm < 0) {
    Serial.println("ERROR: failed to read from Co2 sensor");
  } else {
    Serial.print("DATA: co2="); Serial.print(co2Ppm); Serial.println("PPM");
  }

  // TODO: to separete thread 
  float humidity = dht.getHumidity();
  float tempCelcius = dht.getTemperature();
  if (isnan(humidity) || isnan(tempCelcius)) {
    Serial.println("ERROR: failed to read from DHT sensor");
  } else {
    Serial.print("DATA: humidity="); Serial.print(humidity); Serial.println("%");
    Serial.print("DATA: temperature="); Serial.print(tempCelcius); Serial.println("C");
  }

  delay(5000);
  Serial.println("");
}
