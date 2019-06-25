#!/bin/bash

ERROR="$1"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

if [ -z "$ERROR" ];then
    echo "Usage: $0 error.txt"
    exit 2
fi

"$DIR/EspArduinoExceptionDecoder/decoder.py" \
    -t ~/.platformio/packages/toolchain-xtensa \
    -e "$DIR/.pioenvs/d1/firmware.elf" \
    -f "$1"