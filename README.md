# ambient7

Components:

* [mt8057-agent](#mt8057-agent) agent for co2 concentration & temperature
  measurements. Can be used sepparatly.
* [ambient7-analysis](#ambient7-analysis)
* ambient7-analysis - additional data analisis, scheduled by crontab or similar
* ambient7-webapp - JSON APIs
* ambient7-webui - Web based UI
* influxdb-client, core, rdbms-service - code shared beetwen other components
* [influxdb](influxdb/README.md) as a timeseries db
* [grafana](grafana/README.md) for data explorations (optional)

[![Build Status](https://travis-ci.org/maizy/ambient7.svg?branch=master)](https://travis-ci.org/maizy/ambient7)
[![Coverage Status](https://coveralls.io/repos/github/maizy/ambient7/badge.svg?branch=master)](https://coveralls.io/github/maizy/ambient7?branch=master)

<a name="downloads"></a>
## Downloads

* [ambient7-mt8057-agent.jar](https://github.com/maizy/ambient7/releases/download/0.3.0/ambient7-mt8057-agent-0.3.0.jar)
* [ambient7-analysis.jar](https://github.com/maizy/ambient7/releases/download/0.3.0/ambient7-analysis-0.3.0.jar)
* [ambient7-webapp.jar](https://github.com/maizy/ambient7/releases/download/0.3.0/ambient7-webapp-0.3.0.jar)


<a name="mt8057-agent"></a>
## mt8057-agent

Agent for sending results from
[Masterkit MT8057](http://masterkit.ru/shop/others/dadget/1266110) CO2 detector
(with ZyAura ZG01C module onboard).

Can be used sepparatly without other ambient7 parts as all-in-one fat jar
(jre 1.8+).

Supports various result writers:

* console
* interactive
* InfluxDB API


### Platforms

Supported platforms:

* Mac OS X 10.10+
* Linux amd64

Theoretically supported but never tested (all platforms supported by
[hid4java](https://github.com/gary-rowe/hid4java)):

* Linux ARM
* Linux x86
* Linux x86_64
* Windows 32/64
* Mac OS X 10.5 - 10.9


### Usage

```
java -jar ambient7-mt8057-agent-x.x.x.jar --writers=console

java -jar ambient7-mt8057-agent-x.x.x.jar --writers=interactive

java -jar ambient7-mt8057-agent-x.x.x.jar --writers=influxdb \
    --influxdb-database=ambient7 \
    --influxdb-baseurl=http://localhost:8086/ \
    --influxdb-user=user --influxdb-password=123
```

For more options:

```
java -jar ambient7-mt8057-agent-x.x.x.jar --help
```


<a name="ambient7-analysis"></a>
## ambient7-analysis

Tools for counting aggregates based on data collected in InfluxDb
that cannot be counted by InfluxDb itself.

```
java -jar ambient7-analysis-x.x.x.jar --help
```

### Init or migrate DB

```
java -jar ambient7-analysis-x.x.x.jar --config=ambient7.conf init-db
```

### Co2 hourly report

Add to crontab or any other scheduler the command:

```
java -jar ambient7-analysis-x.x.x.jar --config=ambient7.conf aggregate-co2
```


## Changelog

[See CHANGELOG.md](CHANGELOG.md)


## License

[Apache 2.0](LICENSE.txt)
