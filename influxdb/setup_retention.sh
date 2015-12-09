#!/bin/bash
#
# Add optional retaintion policy to ambient7 db
#
# Usage: ./setup_retention.sh [HOST] [USER:PASS]

HOST="$1"
AUTH="$2"

if [ -z "$HOST" ];then
    HOST='localhost:8086'
fi
AUTH_PARAM=''
if [ -n "$AUTH" ];then
    AUTH_PARAM="-u$AUTH"
fi

function query {
    curl -v -f --get "http://${HOST}/query" \
        "$AUTH_PARAM" \
        --data-urlencode \
        "q=$1"
}

query \
    'DROP CONTINUOUS QUERY co2_mean_per_5min ON ambient7'

query \
    'CREATE CONTINUOUS QUERY co2_mean_per_5min ON ambient7
    BEGIN
        SELECT MEAN(ppm) as ppm INTO ambient7.archive.co2 FROM co2 GROUP BY time(5m), *
    END'

query \
    'DROP CONTINUOUS QUERY temp_mean_per_5min ON ambient7'

query \
    'CREATE CONTINUOUS QUERY temp_mean_per_5min ON ambient7 BEGIN
        SELECT MEAN(celsius) as celsius INTO ambient7.archive.temp FROM temp GROUP BY time(5m), *
    END'

query \
    'ALTER RETENTION POLICY default ON ambient7 DURATION 90d REPLICATION 1 DEFAULT'

echo -e "\nDone"

