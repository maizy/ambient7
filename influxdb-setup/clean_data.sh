#!/bin/bash
# Usage: ./clean_data.sh MEASUREMENT [HOST]

HOST="$2"
MEASUREMENT="$1"

if [ -z "$HOST" ];then
    HOST='localhost:8086'
fi

echo -n "Are you sure to remove all value from measurement '${MEASUREMENT}'"

curl -v -f --get "http://${HOST}/query?db=ambient7" \
    --data-urlencode "q=DROP SERIES FROM ${MEASUREMENT}" \
    && echo -e "\nDone"
