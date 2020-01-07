#include "Arduino.h"
#include "String.h"
#ifndef ESP32
  #include <SoftwareSerial.h>
  #include <ESP8266WiFi.h>
#else
  #include <HardwareSerial.h>
#endif
#include "configs.h"
#ifdef ONLINE_MODE
  #include "Ambient7Wifi.h"
#endif
#include <DHT.h>
#include <MHZ19.h>
#ifdef SEND_TO_INFLUXDB
  #include <InfluxDb.h>
#endif

unsigned long startTime = millis();

MHZ19* mhz19;

#ifndef ESP32
  SoftwareSerial* mhz19Serial;
#else
  HardwareSerial* mhz19Serial;
#endif
DHT* dht;

#ifdef ONLINE_MODE
#ifdef SEND_TO_INFLUXDB
  Influxdb* influxDbClient;
#endif
#endif

void setup()
{
  Serial.begin(9600);
  
  #ifdef ESP32
  Serial.println("Pre init pause: 3s");
  delay(3000);
  #endif

  Serial.println(""); Serial.println("");
  Serial.println("Ambient7");
  Serial.println("https://github.com/maizy/ambient7/");
  Serial.println("");
  Serial.println("Configs: ");
  #ifndef ESP32
    Serial.print("  CO2 RX Pin: "); Serial.println(CO2_RX);
    Serial.print("  CO2 TX Pin: "); Serial.println(CO2_TX);
  #else
    Serial.print("  UART N: "); Serial.println(CO2_UART_NUM);
  #endif
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

  Serial.println("Setup MH-Z19 ...");
  #ifndef ESP32
    mhz19Serial = new SoftwareSerial(CO2_RX, CO2_TX);
  #else
    mhz19Serial = new HardwareSerial(CO2_UART_NUM);
    mhz19Serial->begin(9600, SERIAL_8N1);
  #endif
  mhz19 = new MHZ19();
  mhz19->begin(*mhz19Serial);
  if (CO2_AUTO_CALIBRATION) {
    Serial.println("  Daily auto calibration: on");
  } else {
    Serial.println("  Daily auto calibration: off");
  }
  mhz19->autoCalibration(CO2_AUTO_CALIBRATION);
  mhz19->setRange(2000);

  Serial.println("  Values filter enabled");
  mhz19->setFilter(true, true);
  Serial.println("Done");

  Serial.print("Setup DHT22: ...");
  dht = new DHT(DTH22_1WIRE, DHT22);
  dht->begin();
  Serial.println(" done");

  #ifdef ONLINE_MODE
    setupWifi(WIFI_NETWORK, WIFI_PASSWORD);
    Serial.println("");

    #ifdef SEND_TO_INFLUXDB
      Serial.println("Setup InfluxDB ... ");
      influxDbClient = new Influxdb(INFLUXDB_HOST, INFLUXDB_PORT);
      Serial.print("  InfluxDB host: "); Serial.print(INFLUXDB_HOST);
      Serial.print(":"); Serial.print(INFLUXDB_PORT); Serial.println("");
      Serial.print("  InfluxDB database: "); Serial.println(INFLUXDB_DATABASE);
      if (strlen(INFLUXDB_USER) > 0) {
        influxDbClient->setDbAuth(INFLUXDB_DATABASE, INFLUXDB_USER, INFLUXDB_PASSWORD);
        Serial.print("  InfluxDB user: "); Serial.println(INFLUXDB_USER);
        Serial.print("  InfluxDB password: ");
        for (unsigned int i = 0; i < strlen(INFLUXDB_PASSWORD); i++) {
          Serial.print('*');
        }
        Serial.println("");
      } else {
        influxDbClient->setDb(INFLUXDB_DATABASE);
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

  #ifndef ESP32
    mhz19Serial->enableRx(true);
  #endif

  Serial.print("DATA: uptime="); Serial.print(uptime / 1000); Serial.println("s");
  int co2Ppm = mhz19->getCO2();
  int mhZ19Status = mhz19->errorCode;
  Serial.print("DATA: mh_z19_status="); Serial.println(mhZ19Status);
  if (co2Ppm <= 0 || mhZ19Status != RESULT_OK) {
    Serial.print("ERROR: failed to read from Co2 sensor. Unfiltered value: ");
    Serial.print(co2Ppm); Serial.println("");
  } else {
    Serial.print("DATA: co2="); Serial.print(co2Ppm); Serial.println("PPM");
  }

  #ifndef ESP32
    // disable Interrupt used in software serial,
    // otherwise DHT won't work
    mhz19Serial->enableRx(false);
  #endif

  delay(5000);

  float humidity = dht->readHumidity();
  float tempCelsius = dht->readTemperature();

  int dhtStatus = 0;
  if (isnan(humidity) || isnan(tempCelsius)) {
    Serial.println("ERROR: failed to read from DHT sensor");
    dhtStatus = 1;
  } else if (humidity <= 1.0 && tempCelsius <= 1.0) {
    Serial.print("ERROR: wrong data from DHT sensor. humidity=");
    Serial.print(humidity); Serial.print(", temp=");
    Serial.println(tempCelsius);
    dhtStatus = 2;
  } else {
    Serial.print("DATA: humidity="); Serial.print(humidity); Serial.println("%");
    Serial.print("DATA: temperature="); Serial.print(tempCelsius); Serial.println("C");
  }
  Serial.print("DATA: dht_status="); Serial.println(dhtStatus);

  #ifdef ONLINE_MODE
  #ifdef SEND_TO_INFLUXDB
    String data = String("");
    data += "uptime,device=" + String(INFLUXDB_DEVICE_NAME);
    data += " millis=" + String(uptime, DEC) + "i";

    if (dhtStatus == 0 && !isnan(humidity)) {
      data += "\nhumidity,device=" + String(INFLUXDB_DEVICE_NAME);
      data += " relative=" + String(humidity);
    }
    if (dhtStatus == 0 && !isnan(tempCelsius)) {
      data += "\ntemperature,device=" + String(INFLUXDB_DEVICE_NAME);
      data += " celsius=" + String(tempCelsius);
    }
    data += "\nco2,device=" + String(INFLUXDB_DEVICE_NAME);
    data += " status=" + String(mhZ19Status) + "i";
    if (mhZ19Status == RESULT_OK) {
      data += ",ppm=" + String(co2Ppm, DEC)+"i";
    }
    data += "\ndht_status,device=" + String(INFLUXDB_DEVICE_NAME);
    data += " status=" + String(dhtStatus) + "i";
    influxDbClient->write(data);
  #endif
  #endif
  Serial.println("");
  delay(10000);
}
