#!/bin/bash
#
# InfluxDb docker container setup script
#
# On OS X a data dir can't be a shared vbox disk because
# of vboxfs limitations.
#
# Usage:
#   cd ambient7/influxdb-setup/docker
#   ./influxdb_setup.sh DATADIR ADMIN_PASSWORD
#
DATADIR="$1"
ADMIN_PASSWORD="$2"  # login "root"
API_PORT='8086'
ADMIN_PORT='8083'

if [[ -z "$DATADIR" || -z "$ADMIN_PASSWORD"  ]];then
    echo "Usage: `basename $0` DATADIR ADMIN_PASSWORD"
    exit 1
fi

docker run -d -p "${API_PORT}:8086" -p "${ADMIN_PORT}:8083" \
    -v "${DATADIR}:/data" \
    -e ADMIN_USER="root" \
    -e "INFLUXDB_INIT_PWD=${ADMIN_PASSWORD}" \
    -e "PRE_CREATE_DB=ambient7" \
    --name=ambient7-influxdb \
    ambient7/influxdb:0.1
