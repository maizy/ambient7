
# Extras


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
* macOS: `screen /dev/tty.wchusbserial1410 115200`
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
