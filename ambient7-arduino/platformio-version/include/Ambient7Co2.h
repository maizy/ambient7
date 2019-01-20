#pragma once

#include <SoftwareSerial.h>

extern int readCO2UART(SoftwareSerial co2Serial, unsigned int timeoutMillis = 5000);
