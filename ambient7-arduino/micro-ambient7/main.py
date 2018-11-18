# import machine
# import time

# pin = machine.Pin(2, machine.Pin.OUT)

# def toggle(p):
#     p.value(not p.value())

# while True:
#     toggle(pin)
#     time.sleep_ms(500)

import ambient7_config
import time

def do_connect():
    import network
    sta_if = network.WLAN(network.STA_IF)
    if not sta_if.isconnected():
        print('connecting to network {} ...'.format(ambient7_config.WIFI_NETWORK))
        sta_if.active(True)
        sta_if.connect(ambient7_config.WIFI_NETWORK, ambient7_config.WIFI_PASSWORD)
        while not sta_if.isconnected():
            time.sleep_ms(500)
            print('.')
    return sta_if

sta_if = do_connect()

base_page = """<!DOCTYPE html>
<html>
    <head><title>ambient7 on MCU</title></head>
    <body>
    {}
    </body>
</html>"""

import socket
addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]

s = socket.socket()
s.bind(addr)
s.listen(1)

print('listening on', addr)

while True:
    cl, addr = s.accept()
    print('client connected from', addr)
    cl_file = cl.makefile('rwb', 0)
    while True:
        line = cl_file.readline()
        if not line or line == b'\r\n':
            break
    status = '<pre>{}</pre>'.format(sta_if.ifconfig())
    response = base_page.format(status)
    cl.send(response)
    cl.close()
