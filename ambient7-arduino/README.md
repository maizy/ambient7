# Ambient7 with Arduino compatible MCU

## Hardware

* MCU - Wemos D1 R1
* Temperature/Humidity sensor – Asair (aka Aosong) AM2302 (DHT22)
* CO2 Sensor – Winsen MH-Z19B

All components may be replaced with analogues.

## Implementations

* [PlatformIO based](platformio-version)
* [Arduino IDE based](arduino-ide-version) – abandoned. Arduino IDE is ugly.
* [micropython](micropython-version/) – abandoned. lack of
  software serial (UART) support.

## Extra apps

* [hello-world-led](hello-world-led/) – test arduino sketch

## Wemos D1 R1 setup

### PlatformIO

* install PlatformIO (`brew install platformio` on macOS)
* change `platformio.ini` if you use other hardware
* setup `platformio-version/include/configs.h` (copy from `configs.h.example`)
* build project and upload
```
cd platformio-version
platformio run --target upload
```
* serial monitor:
```
cd platformio-version
platformio device monitor -b 115200
```


### ArduinoIDE

* install [USB-Serial driver](https://wiki.wemos.cc/downloads)
  * connect board
  * check tty at `ls /dev/*serial*`
* install Arduino IDE
* Arduino IDE -> Preference -> Additional Boards Manager URLs:
  `http://arduino.esp8266.com/stable/package_esp8266com_index.json`
* Arduino IDE -> Tools -> Board -> Boards Manager -> install
  `esp8266 by ESP8266 Community`

### Reading from Serial Port

* ArduinoIDE -> Serial Monitor
* macOS: `screen /dev/tty.wchusbserial1410 9600`
  * `Control-a k` to exit

### Reset firmware, install micropython

Erase flash:

```
esptool.py --port /dev/tty.wchusbserial1410 erase_flash
```

Install micropython firmware:

```
esptool.py --port /dev/tty.wchusbserial1410 \
  --baud 115200 \
  write_flash --flash_size=detect 0 \
  /path/to/micropython/esp8266-20180511-v1.9.4.bin
```
