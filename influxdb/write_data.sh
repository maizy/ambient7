#!/bin/bash
# Usage: ./write_data.sh FILE [HOST] [USER:PASS]

FILE="$1"
HOST="$2"
AUTH="$3"

if [ ! -f "$FILE" ]; then
    echo "Usage ./write_data.sh FILE [HOST] [USER:PASS]"
    exit 1
fi

if [ -z "$HOST" ];then
    HOST='localhost:8086'
fi
AUTH_PARAM=''
if [ -n "$AUTH" ];then
    AUTH_PARAM="-u$AUTH"
fi

curl -v -f -X POST "http://${HOST}/write?db=ambient7" \
    --data-binary "@$FILE" \
    "$AUTH_PARAM" \
    && echo -e "\nDone"
