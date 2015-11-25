#!/bin/bash
# Usage: ./create_db.sh [HOST]

HOST="$1"
if [ -z "$HOST" ];then
    HOST='localhost:8086'
fi

curl -v -f --get "http://${HOST}/query" \
    --data-urlencode "q=CREATE DATABASE IF NOT EXISTS ambient7" \
    && echo -e "\nDone"
