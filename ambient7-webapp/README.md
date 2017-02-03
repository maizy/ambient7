# Ambient7 WebApp

<a name="dev_run"></a>
## Dev run

```
cd ambient7/
sbt 'ambient7WebApp/run --config=config/ambient7.conf'
```

or with additional settings:
```
sbt -Dlogback.configurationFile=ambient7-webapp/src/etc/logback.dev.xml \
    'ambient7WebApp/run --config=config/ambient7.conf'
```

Open [http://localhost:22480/](http://localhost:22480/) in your browser.


## Build

* compile [ambient7-webui](../ambient7-webui) assets
  * `cd ambient7-webui`
  * `npm install`
  * `npm run release`
* `sbt ~ambient7WebApp/assembly`
