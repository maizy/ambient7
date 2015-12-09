#!/usr/bin/env python
# coding: utf-8
#
# Convert console writer logs to InfluxDB line protocols
# Usage: python convert_console_logs.py LOGPATH [AGENTNAME]
#
import sys
import os
import re
import datetime
import time

LINE_RE = re.compile(r'(?P<time>^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}):'
                     r' (?P<type>temp|co2)=(?P<value>[\d\.]+)')


def escape(value):
    return value.replace(' ', '\\ ').replace(',', '\\,')


def convert(f, agent):
    tags = ',agent={}'.format(escape(agent))
    for line in f:
        match_res = LINE_RE.match(line.rstrip('\n'))
        if match_res:
            converter = None
            value = None
            if match_res.group('type') == 'temp':
                value = float(match_res.group('value'))
                converter = format_temp
            elif match_res.group('type') == 'co2':
                value = int(match_res.group('value'))
                converter = format_co2

            if converter is not None:
                ts = parse_timestamp(match_res.group('time'))
                print(converter(value, tags, ts))


def format_temp(celsius, tags, ts):
    return 'temp{t},device=mt8057 celsius={c:.4f} {ts}'.format(c=celsius, t=tags, ts=ts)


def format_co2(ppm, tags, ts):
    return 'co2{t},device=mt8057 ppm={p}i {ts}'.format(p=ppm, t=tags, ts=ts)


def parse_timestamp(formated_time):
    ts = time.mktime(datetime.datetime.strptime(formated_time, '%Y-%m-%d %H:%M:%S').timetuple()) * 10**9
    return '{:.0f}'.format(ts)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage {} LOGPATH [AGENTNAME]'.format(os.path.basename(__file__)))
        sys.exit(1)
    file_path = sys.argv[1]
    agent = 'main' if len(sys.argv) < 3 else sys.argv[2]
    with open(file_path, 'rb') as f:
        convert(f, agent)
