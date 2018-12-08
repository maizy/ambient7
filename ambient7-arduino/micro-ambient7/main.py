import time
import socket
import machine
import dht
import network

import ambient7_config


def init_wifi():
    sta_if = network.WLAN(network.STA_IF)
    if not sta_if.isconnected():
        print('connecting to network {} ...'.format(ambient7_config.WIFI_NETWORK))
        sta_if.active(True)
        sta_if.connect(ambient7_config.WIFI_NETWORK, ambient7_config.WIFI_PASSWORD)
        while not sta_if.isconnected():
            time.sleep_ms(500)
            print('.')
    print('wifi ready')
    print(format_network_status(sta_if.ifconfig()))
    return sta_if


def format_network_status(status):
    if not status or len(status) != 4:
        return 'Bad network status'
    lines = []
    for i, field in enumerate(('ip', 'netmask', 'gateway', 'dns')):
        lines.append('{}: {}'.format(field, status[i]))
    return '\n'.join(lines)


def start_http_server():
    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]

    server_socket = socket.socket()
    server_socket.bind(addr)
    server_socket.listen(1)

    print('listening on {}'.format(addr))
    return server_socket


PAGE_TEMPLATE = """<!DOCTYPE html>
<html>
    <head><title>ambient7 on MCU</title></head>
    <body>

    <h3>Wifi status</h3>
    <pre>{wifi}</pre>

    <h3>DHT</h3>

    <ul>
        <li>Temperature: {t}˚C</li>
        <li>Humidity: {h}%RH</li>
    </ul>

    </body>
</html>"""

dht_pin = machine.Pin(ambient7_config.DHT_1WIRE_PIN)
dht = dht.DHT22(dht_pin)


def make_response(sta_if, request_content):
    try:
        dht.measure()
        dht_ready = True
    except OSError:
        dht_ready = False

    values = {
        'wifi': format_network_status(sta_if.ifconfig()),
        't': '{:.2f}'.format(dht.temperature()) if dht_ready else 'error',
        'h': '{:.2f}'.format(dht.humidity()) if dht_ready else 'error',
    }
    return PAGE_TEMPLATE.format(**values)


def main_loop(server_socket, sta_if):

    while True:
        client_socket, client_addr = server_socket.accept()
        print('client connected from {}'.format(client_addr))
        request = client_socket.makefile('rwb', 0)
        request_content = []
        while True:
            line = request.readline()
            if line:
                request_content.append(line)
            if not line or line == b'\r\n':
                break

        response = make_response(sta_if, request_content)

        client_socket.send(response)
        client_socket.close()

sta_if = init_wifi()
server_socket = start_http_server()
main_loop(server_socket, sta_if)
