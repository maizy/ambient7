#pragma once
/*
Project configuration.
*/

const int WAIT_SECONDS = 300;

#ifndef ESP32
  const byte CO2_RX = D6;  // UART RX pin = sensor TX
  const byte CO2_TX = D7;  // UART TX pin = sensor RX
#else
  const byte CO2_UART_NUM = 2;
#endif
