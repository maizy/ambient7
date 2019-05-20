#include "Arduino.h"
#include "String.h"
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
#ifdef SEND_TO_INFLUXDB
  #include <InfluxDb.h>
#endif

unsigned long startTime = millis();

MHZ19_uart mhz19;
DHT dht;

#ifdef SEND_TO_INFLUXDB
  Influxdb influxDbClient(INFLUXDB_HOST, INFLUXDB_PORT);
#endif

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

  Serial.print("Setup MH-Z19 ...");
  mhz19.begin(CO2_RX, CO2_TX);
  mhz19.setAutoCalibration(false);
  Serial.println(" done");

  #ifdef ONLINE_MODE
    setupWifi(WIFI_NETWORK, WIFI_PASSWORD);
    Serial.println("");

    #ifdef SEND_TO_INFLUXDB
      Serial.println("Setup InfluxDB ... ");
      Serial.print("  InfluxDB host: "); Serial.print(INFLUXDB_HOST);
      Serial.print(":"); Serial.print(INFLUXDB_PORT); Serial.println("");
      Serial.print("  InfluxDB database: "); Serial.println(INFLUXDB_DATABASE);
      if (strlen(INFLUXDB_USER) > 0) {
        influxDbClient.setDbAuth(INFLUXDB_DATABASE, INFLUXDB_USER, INFLUXDB_PASSWORD);
        Serial.print("  InfluxDB user: "); Serial.println(INFLUXDB_USER);
        Serial.print("  InfluxDB password: ");
        for (unsigned int i = 0; i < strlen(INFLUXDB_PASSWORD); i++) {
          Serial.print('*');
        }
        Serial.println("");
      } else {
        influxDbClient.setDb(INFLUXDB_DATABASE);
      }
      Serial.println("Done");
    #endif

  #else
    WiFi.mode(WIFI_OFF);
  #endif

  Serial.println("");
  Serial.println("");
}

void loop()
{
  unsigned long uptime = millis() - startTime;
  Serial.print("DATA: uptime="); Serial.print(uptime / 1000); Serial.println("s");
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
  float tempCelsius = dht.getTemperature();
  if (isnan(humidity) || isnan(tempCelsius)) {
    Serial.println("ERROR: failed to read from DHT sensor");
  } else {
    Serial.print("DATA: humidity="); Serial.print(humidity); Serial.println("%");
    Serial.print("DATA: temperature="); Serial.print(tempCelsius); Serial.println("C");
  }

  #ifdef SEND_TO_INFLUXDB
    String data = String("");
    data += "uptime,device=" + String(INFLUXDB_DEVICE_NAME);
    data += " millis=" + String(uptime, DEC) + "i";

    if (!isnan(humidity)) {
      data += "\nhumidity,device=" + String(INFLUXDB_DEVICE_NAME);
      data += " relative=" + String(humidity);
    }
    if (!isnan(tempCelsius)) {
      data += "\ntemperature,device=" + String(INFLUXDB_DEVICE_NAME);
      data += " celsius=" + String(tempCelsius);
    }
    data += "\nco2,device=" + String(INFLUXDB_DEVICE_NAME);
    data += " status=" + String(mhZ19Status) + "i";
    if (co2Ppm > 0) {
      data += ",ppm=" + String(co2Ppm, DEC);
    } else {
      data += ",ppm=0i";
    }
    influxDbClient.write(data);
  #endif

  delay(5000);
  Serial.println("");
}
