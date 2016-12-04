# Upgrade from influxdb 0.9 (ambient7 v0.1/v0.2) to influxdb 1.1 (ambient7 v0.3)

* stop `ambient7-influxdb` container (`docker stop ambient7-influxdb`)
* backup all data at host machine (recommended)
* build upgrade container - `cd upgrade_0-9_1-1; ./influxdb_upgrade_build.sh`
* setup upgrade container - `cd upgrade_0-9_1-1; ./influxdb_upgrade_setup.sh /data`
  with the same data dir as used before in ambient7-influxdb
* start bash session `docker exec -ti ambient7-influxdb-upgrade /bin/bash`
```
# convert shards
mkdir /data/backup
influx_tsm -backup /data/backup -parallel /data/db
# confirm with Y

# convert meta data
exec /usr/bin/influxd -config=/config/config.toml &
# press enter
mkdir -p /data/backup/meta
influxd backup /data/backup/meta/
killall influxd
exit
```
* stop & remove upgrade container (`docker rm -f ambient7-influxdb-upgrade`)
* remove old ambient7-influxdb container (`docker rm ambient7-influxdb`)
* build new influxdb image (`cd docker; ./influxdb_build.sh`)
* setup new influxdb image with the same data dir and password (`cd docker; ./influxdb_setup.sh /data password`)
* start bash session `docker exec -ti ambient7-influxdb /bin/bash`
```
# stop influxdb
sv stop influxdb
influxd restore -metadir=/data/meta /data/backup/meta/
exit
```
* restart docker container (`docker restart ambient7-influxdb`)
* check logs `docker logs ambient7-influxdb`
