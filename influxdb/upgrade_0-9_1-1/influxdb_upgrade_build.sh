#!/bin/bash
#
# Usage:
#   cd ambient7/influxdb/upgrade_0-9_1-1
#   ./influxdb_build.sh
#
docker build --tag ambient7/influxdb_upgrade:0.1 ./
