# InfluxDB

## Docker image

Built over phusion/baseimage-docker
Based on https://github.com/tutumcloud/influxdb

### Usage

```bash
cd ambient7/influxdb/docker
./influxdb_build.sh
./influxdb_setup.sh /mnt/data/influxdb adminpassword
```

### Logs

```bash
docker logs ambient7-influxdb
docker logs -f ambient7-influxdb
```
