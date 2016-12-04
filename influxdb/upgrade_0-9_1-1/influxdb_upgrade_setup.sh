#!/bin/bash
#
# Usage:
#   cd ambient7/influxdb/upgrade_0-9_1-1
#   ./influxdb_upgrade_setup.sh DATADIR
#
DATADIR="$1"

if [[ -z "$DATADIR" ]];then
    echo "Usage: `basename $0` DATADIR"
    exit 1
fi

docker run -d \
    -v "${DATADIR}:/data" \
    -e ADMIN_USER="root" \
    --name=ambient7-influxdb-upgrade \
    ambient7/influxdb_upgrade:0.1
