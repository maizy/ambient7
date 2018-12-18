#!/usr/bin/env python3
import argparse
import sys
import inspect
import os.path

from plumbum import local
from plumbum.commands.processes import CommandNotFound

AMPY_ARGS = (
    '--delay', '0.5',
    '--baud', '115200',
    '--port', '/dev/tty.wchusbserial1410')

ESPTOOL_ARGS = (
    '--baud', '115200',
    '--port', '/dev/tty.wchusbserial1410')

EXIT_UNKNOWN = 1
EXIT_REQUIREMENTS = 2
EXIT_CONFIG = 3

PROJECT_DIR = os.path.realpath(os.path.dirname(__file__))
CODE_DIR = os.path.join(PROJECT_DIR, 'micro-ambient7')


def get_ampy():
    try:
        ampy = local['ampy']
    except CommandNotFound as e:
        print('ampy command not found', file=sys.stderr)
        sys.exit(EXIT_REQUIREMENTS)
    return ampy[AMPY_ARGS]


def get_esptool():
    try:
        esptool = local['esptool.py']
    except CommandNotFound as e:
        print('esptool.py command not found', file=sys.stderr)
        sys.exit(EXIT_REQUIREMENTS)
    return esptool[ESPTOOL_ARGS]


def check_config():
    if not os.path.exists(os.path.join(PROJECT_DIR, 'config.py')):
        print(
            'config.py not found\n\n'
            'copy config.py.example to config.py\n',
            'edit config.py',
            file=sys.stderr)
        sys.exit(EXIT_REQUIREMENTS)


def run_cmd(cmd):
    print('>> {}'.format(cmd))
    print(cmd())


class Command:

    @staticmethod
    def parser(subparser):
        pass


class reboot(Command):
    help = 'board soft reboot'

    def exec(self, args):
        ampy = get_ampy()
        cmd = ampy['reset']
        run_cmd(cmd)

# TODO: flag to remove other files
class sync(Command):
    help = 'sync code to board'

    def exec(self, args):
        check_config()
        ampy = get_ampy()
        # FIXME why it's not working?
        # run_cmd(ampy['put', CODE_DIR])

        for f in ('boot.py', 'main.py'):
            run_cmd(ampy['put', os.path.join(CODE_DIR, f), f])

        run_cmd(ampy['put', os.path.join(PROJECT_DIR, 'config.py'), 'ambient7_config.py'])


class board_status(Command):
    help = 'get board status'

    def exec(self, args):
        esptool = get_esptool()
        run_cmd(esptool['read_flash_status'])


COMMANDS = [c for c in locals().values() if inspect.isclass(c) and issubclass(c, Command)]


def parse_args():
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(help='command --help', dest='command', required=True)
    for command in COMMANDS:

        subparser = subparsers.add_parser(
            command.__name__,
            help=command.help if hasattr(command, 'help') else None
        )
        command.parser(subparser)

    return parser.parse_args()


if __name__ == '__main__':
    args = parse_args()
    command_cls = next(c for c in COMMANDS if c.__name__ == args.command)
    command = command_cls()
    command.exec(args)
