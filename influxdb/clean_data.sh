#!/bin/bash
# Usage: ./clean_data.sh MEASUREMENT [HOST] [USER:PASS]

MEASUREMENT="$1"
HOST="$2"
AUTH="$3"

if [ -z "$HOST" ];then
    HOST='localhost:8086'
fi
AUTH_PARAM=''
if [ -n "$AUTH" ];then
    AUTH_PARAM="-u$AUTH"
fi
if [ -z "$MEASUREMENT" ];then
    echo "Usage: `basename $0` MEASUREMENT [HOST] [USER:PASS]"
    exit 1
fi

echo -n "Are you sure to remove all value from measurement '${MEASUREMENT}' [N/y] "
read Y

if [[ $Y == "Y" || $Y == "y" ]];then
    curl -v -f -XPOST "http://${HOST}/query?db=ambient7" \
        "$AUTH_PARAM" \
        --data-urlencode "q=DROP SERIES FROM ${MEASUREMENT}" \
        && echo -e "\nDone"
else
    echo "Canceled"
fi

