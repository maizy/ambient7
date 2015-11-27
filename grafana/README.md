# Grafana

Optional component.


## Installation

Setup docker container:

```
./grafana_setup.sh /mnt/data/grafana pa$$word
```

Add an influxdb user with read permissions.

```
CREATE USER "grafana" WITH PASSWORD 'password';
GRANT READ ON "ambient7" TO "grafana";
```
