# ambient7

Components:

* [influxdb](influxdb/README.md) as timeseries db
* [grafana](grafana/README.md) for data explorations (optional)
* [mt8057-agent](#mt8057-agent) agent for co2 concentration & temperature
  measurements. Can be used sepparatly.


<a name="mt8057-agent" />
## mt8057-agent

Agent for sending results from
[Masterkit MT8057](http://masterkit.ru/shop/others/dadget/1266110) CO2 detector
(with ZyAura ZG01C module onboard).

Can be used sepparatly without other ambient7 parts as all-in-one fat jar
(jre 1.6+).

Supports various result writers:

* console
* interactive
* InfluxDB API


### Download

[ambient7-mt8057-agent-0.1.0.jar](https://github.com/maizy/ambient7/releases/download/0.1.0/ambient7-mt8057-agent-0.1.0.jar)


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
java -jar ambient7-mt8057-agent-x.x.x.jar --writers console

java -jar ambient7-mt8057-agent-x.x.x.jar --writers interactive

java -jar ambient7-mt8057-agent-x.x.x.jar --writers influxdb \
    --influxdb-database ambient7 \
    --influxdb-baseurl http://localhost:8086/write \
    --influxdb-user user --influxdb-password 123
```

For more options:

```
java -jar ambient7-mt8057-agent-x.x.x.jar --help
```


## Changelog

[See CHANGELOG.md](CHANGELOG.md)


## License

[The MIT License](LICENSE.txt)
