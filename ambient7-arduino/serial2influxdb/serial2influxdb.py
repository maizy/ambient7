#!/usr/bin/env python3
# coding: utf-8
import argparse
import configparser
import logging
import time

import serial


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
                timeout=1
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


def main(args):
    cli_args, config = parse_args_and_config(args)

    logging.basicConfig(
        level=logging.DEBUG if cli_args.verbose else logging.INFO,
        stream=sys.stderr,
        format='%(asctime)s %(levelname).1s %(message)s'
    )

    for line in resilient_line_generator(config):
        logger.debug(line)

    return 0


if __name__ == '__main__':
    import sys
    sys.exit(main(sys.argv[1:]))
