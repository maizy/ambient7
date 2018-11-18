# Ambient7 with Arduino compatible MCU

## Wemos D1 setup for ArduinoIDE

* install [USB-Serial driver](https://wiki.wemos.cc/downloads)
  * connect board
  * check tty at `ls /dev/*serial*`
* install Arduino IDE
* Arduino IDE -> Preference -> Additional Boards Manager URLs:
  `http://arduino.esp8266.com/stable/package_esp8266com_index.json`
* Arduino IDE -> Tools -> Board -> Boards Manager -> install
  `esp8266 by ESP8266 Community`

## Reading from Serial Port

* ArduinoIDE -> Serial Monitor
* `screen /dev/tty.wchusbserial1410 9600`
  * `Control-a k` to exit
