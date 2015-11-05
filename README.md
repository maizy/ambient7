# ambient7

_TBA_

## mt8057-agent

Agent for sending results from
[Masterkit MT8057](http://masterkit.ru/shop/others/dadget/1266110) CO2 detector
(with ZyAura ZG01C module onboard).

Can be used sepparatly without other ambient7 parts as all-in-one fat jar
(jre 1.6+).

Supports various result writers:

* console
* interactive
* InfluxDB API (*TODO*)


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
```

For more options:

```
java -jar ambient7-mt8057-agent-x.x.x.jar --help
```


## Changelog

[See CHANGELOG.md](CHANGELOG.md)


## License

[The MIT License](LICENSE.txt)
