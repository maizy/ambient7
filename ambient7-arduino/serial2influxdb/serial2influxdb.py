#!/usr/bin/env python3
# coding: utf-8
import argparse
import configparser
import logging
import time
import re
import datetime

import serial
import influxdb


SERIAL_RETRY_DELAY = 5.0

logger = logging.getLogger('s2i')


def parse_args_and_config(args):
    parser = argparse.ArgumentParser(description='ambient7 - serial2influxdb')
    parser.add_argument('-v', '--verbose', action='store_true')

    parser.add_argument('config', metavar='config.cfg', type=argparse.FileType('r', encoding='UTF-8'),
                        default='./config.cfg', help='path to config', nargs='?')
    cli_args = parser.parse_args(args)
    config = configparser.ConfigParser()
    config.read_file(cli_args.config)
    return cli_args, config


def open_serial(config):
    while True:
        try:
            return serial.Serial(
                port=config['serial']['tty'],
                baudrate=int(config['serial']['baud']),
                timeout=1,
                exclusive=True
            )
        except serial.SerialException as e:
            logger.warning('unable to open pyserial connection: {}'.format(e))
            logger.info('retry after {} second'.format(SERIAL_RETRY_DELAY))
            time.sleep(SERIAL_RETRY_DELAY)


def resilient_line_generator(config):
    ser = None
    while True:
        if ser is None:
            ser = open_serial(config)
        try:
            byte_str = ser.readline()
        except serial.SerialException as e:
            try:
                ser.close()
            except Exception:
                pass
            ser = None
            continue
        if byte_str not in (b'', b'\r\n'):
            try:
                yield byte_str.decode('utf-8').rstrip('\r\n')
            except UnicodeDecodeError:
                pass


def collect_data(key, value, tags=None):
    data = {
        'time': datetime.datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ'),
        'measurement': key,
    }
    if tags:
        data['tags'] = tags
    if key == 'uptime':
        data['fields'] = {'value': int(value.rstrip('s'))}
    elif key == 'humidity':
        data['fields'] = {'value': float(value.strip('%'))}
    elif key == 'co2':
        if value.endswith('PPM'):
            value = value[:-3]
        data['fields'] = {'value': int(value)}
    elif key == 'temperature':
        data['fields'] = {'value': float(value.strip('C'))}
    else:
        return None
    return [data]


def build_influxdb_client(config):
    opts = {
        'host': config['influxdb']['server'],
        'port': int(config['influxdb']['port']),
        'database': config['influxdb']['database']
    }
    if 'username' in config['influxdb']:
        opts['username'] = config['influxdb']['username']
        opts['password'] = config['influxdb']['password']
    return influxdb.InfluxDBClient(**opts)


def main(args):
    cli_args, config = parse_args_and_config(args)

    influxdb_client = build_influxdb_client(config)

    logging.basicConfig(
        level=logging.DEBUG if cli_args.verbose else logging.INFO,
        stream=sys.stderr,
        format='%(asctime)s %(levelname).1s %(message)s'
    )

    tags = {}
    if 'metrics' in config and 'tags' in config['metrics']:
        for pair in config['metrics']['tags'].split(','):
            tag_key, _, tag_value = pair.partition('=')
            tags[tag_key] = tag_value

    try:
        for line in resilient_line_generator(config):
            if logger.isEnabledFor(logging.DEBUG):
                logger.debug("receive line: %r", line)
            data_match = re.match(r'DATA: (?P<key>[a-z0-9_]+)=(?P<value>.+)', line, re.IGNORECASE)
            if data_match is not None:
                key = data_match.group('key')
                raw_value = data_match.group('value')
                logging.info('%s=%s', key, raw_value)

                data = collect_data(key, raw_value, tags)
                if data is not None:
                    influxdb_client.write_points(data)
    except KeyboardInterrupt:
        return 1
    return 0


if __name__ == '__main__':
    import sys
    sys.exit(main(sys.argv[1:]))
