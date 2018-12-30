#include "Arduino.h"
#include <ESP8266WiFi.h>
#include <WiFiClient.h>

void setupWifi(const char* network, const char* password)
{
    WiFi.mode(WIFI_STA);
    WiFi.begin(network, password);
    Serial.print("Connecting to wifi: ");

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println(" done");

    Serial.println("Connected to WIFI network");
    Serial.print("IP address: "); Serial.println(WiFi.localIP());
}
