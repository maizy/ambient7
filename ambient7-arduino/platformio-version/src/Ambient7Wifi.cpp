#include "Arduino.h"
#include <ESP8266WiFi.h>
#include <WiFiClient.h>

void setupWifi(const char* network, const char* password)
{
    WiFi.mode(WIFI_STA);
    WiFi.begin(network, password);
    Serial.print("Connecting to wifi: ...");

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.print(" done  IP: "); Serial.print(WiFi.localIP());
    Serial.print(" Gateway: "); Serial.println(WiFi.gatewayIP());
    Serial.println("");
}
