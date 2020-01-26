#include "Arduino.h"
#include "String.h"
#ifndef ESP32
  #include <SoftwareSerial.h>
#else
  #include <HardwareSerial.h>
#endif
#include "configs.h"
#include <MHZ19.h>

unsigned long startTime = -1;

MHZ19* mhz19;

#ifndef ESP32
  SoftwareSerial* mhz19Serial;
#else
  HardwareSerial* mhz19Serial;
#endif

bool calibratingNow = false;
bool calibrated = false;


void setup()
{
  Serial.begin(9600);

  #ifdef ESP32
  Serial.println("Pre init pause: 3s");
  delay(3000);
  #endif

  Serial.println(""); Serial.println("");
  Serial.println("Ambient7 manual calibration");
  Serial.println("https://github.com/maizy/ambient7/");
  Serial.println("");
  Serial.println("Configs: ");
  #ifndef ESP32
    Serial.print("  CO2 RX Pin: "); Serial.println(CO2_RX);
    Serial.print("  CO2 TX Pin: "); Serial.println(CO2_TX);
  #else
    Serial.print("  UART N: "); Serial.println(CO2_UART_NUM);
  #endif
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
  mhz19->setRange(2000);

  Serial.println("  Values filter enabled");
  mhz19->setFilter(true, true);
  Serial.println("Done");

  Serial.println("");
  startTime = millis();
}

void loop()
{
  unsigned long running = millis() - startTime;
  Serial.print("Running time: "); Serial.print(running / 1000); Serial.println("s");

  if (!calibrated) {
    long remain = WAIT_SECONDS - (running / 1000);
    Serial.print("MH-Z19 calibration will start after ");
    Serial.print(remain);
    Serial.println(" seconds");

    if (remain <= 0) {
      calibratingNow = true;
    }
  } else {
    Serial.println("MH-Z19 was calibrated.");
  }

  int co2Ppm = mhz19->getCO2();
  int mhZ19Status = mhz19->errorCode;
  Serial.print("MH-Z19 status: "); Serial.println(mhZ19Status);
  if (co2Ppm <= 0 || mhZ19Status != RESULT_OK) {
    Serial.print("ERROR: failed to read from Co2 sensor. Unfiltered value: ");
    Serial.print(co2Ppm); Serial.println("");
  } else {
    Serial.print("CO2: "); Serial.print(co2Ppm); Serial.println("PPM");
  }
  Serial.println("");

  if (calibratingNow) {
    calibratingNow = false;
    Serial.println("Calibrating. Current value will set as 400PPM");

    mhz19->calibrateZero();
    mhz19->setSpan(2000);
    mhz19->autoCalibration(false);

    Serial.println("Done");
    Serial.println("");
    calibrated = true;
  }

  delay(15000);

}
