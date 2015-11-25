#!/bin/bash
# Usage: ./write_data.sh FILE [HOST]

HOST="$2"
FILE="$1"

if [ ! -f "$FILE" ]; then
    echo "Usage ./write_data.sh FILE [HOST]"
    exit 1
fi

if [ -z "$HOST" ];then
    HOST='localhost:8086'
fi

curl -v -f -X POST "http://${HOST}/write?db=ambient7" \
    --data-binary "@$FILE" \
    && echo -e "\nDone"
