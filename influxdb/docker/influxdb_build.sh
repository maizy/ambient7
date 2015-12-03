#!/bin/bash
#
# InfluxDb docker build container script
#
# Usage:
#   cd ambient7/influxdb-setup/docker
#   ./influxdb_build.sh
#
docker build --tag ambient7/influxdb:0.1 ./
