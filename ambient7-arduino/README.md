# Ambient7 with Arduino compatible MCU

## Hardware

* MCU - Wemos D1 R1/R2
* Temperature/Humidity sensor – Asair (aka Aosong) AM2302 (DHT22)
* CO2 Sensor – Winsen MH-Z19B

All components may be replaced with analogues.

## Implementations

* main: PlatformIO based
* [Arduino IDE based](extras/arduino-ide-version) – abandoned. Arduino IDE is ugly.
* [micropython](extras/micropython-version) – abandoned. lack of
  software serial (UART) support.

## Extra apps

* [hello-world-led](extras/hello-world-led/) – test arduino sketch

## Wemos D1 R1/R2 setup

* install [USB-Serial driver](https://wiki.wemos.cc/downloads)
  * connect board
  * check tty at `ls /dev/*serial*`
* [install PlatformIO](https://docs.platformio.org/en/latest/installation.html) (`brew install platformio` on macOS)
* change `platformio.ini` if you use other hardware
* setup `include/configs.h` (copy from `configs.h.example`)
* build project and upload
```
cd ambient7-arduino
platformio run --environment=r2 --target upload
```
* serial monitor:
```
cd ambient7-arduino
platformio device monitor --environment=r2 -b 9600
```
