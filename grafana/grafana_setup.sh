#!/bin/bash
#
# Grafana docker container setup script
#
#
# Usage: ./grafana_setup.sh DATADIR ADMIN_PASSWORD [PORT]
#

DATADIR="$1"
ADMIN_PASSWORD="$2"
PORT="$3"

if [[ -z "$DATADIR" || -z "$ADMIN_PASSWORD"  ]];then
    echo "Usage: `basename $0` DATADIR ADMIN_PASSWORD [PORT]"
    exit 1
fi
if [ -z "$PORT" ];then
    PORT="3000"
fi

docker run -d -p "${PORT}:3000" \
    -v "${DATADIR}:/var/lib/grafana" \
    -e "GF_SECURITY_ADMIN_PASSWORD=${ADMIN_PASSWORD}" \
    --name=ambient7-grafana \
    grafana/grafana:latest
