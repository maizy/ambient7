// based on maple37 code from
// https://forum.arduino.cc/index.php?topic=525459.msg3587557#msg3587557

#include "Arduino.h"
#include <SoftwareSerial.h>

byte countCheckSum(byte *packet)
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

// TODO: return struct with all results
int readCO2UART(SoftwareSerial co2Serial, unsigned int timeoutMillis = 5000)
{
    byte cmd[9] = {0xFF, 0x01, 0x86, 0x00, 0x00, 0x00, 0x00, 0x00, 0x79};
    byte response[9]; // for answer

    Serial.println("Sending CO2 request...");
    co2Serial.write(cmd, 9); //request PPM CO2

    // clear the buffer
    memset(response, 0, 9);

    int elapsed = 0;
    int delayMillis = 500;
    while (co2Serial.available() == 0) {
        delay(delayMillis);
        elapsed += delayMillis;
        if (elapsed > timeoutMillis) {
            Serial.println("ERROR: Unable to get Co2 data");
            return -1;
        }
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
    byte check = countCheckSum(response);
    if (response[8] != check) {
        Serial.println("Checksum not OK!");
        Serial.print("Received: ");
        Serial.println(response[8]);
        Serial.print("Should be: ");
        Serial.println(check);
        return -2;
    }

    // ppm
    int ppm_uart = 256 * (int)response[2] + response[3];
    Serial.print("PPM: "); Serial.println(ppm_uart);

    // status
    byte status = response[5];
    Serial.print("Status: "); Serial.println(String(status, HEX));
    if (status == 0x40) {
        Serial.println("Status OK");
    }

    return ppm_uart;
}
