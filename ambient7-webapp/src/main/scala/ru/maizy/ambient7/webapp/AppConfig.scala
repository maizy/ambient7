package ru.maizy.ambient7.webapp

import java.time.ZoneId
import ru.maizy.ambient7.core.config.Defaults
import ru.maizy.ambient7.webapp.data.Co2Device

case class AppConfig(
    // FIXME: migrate to uni config
    dbUrl: String = Defaults.DB_URL,
    dbUser: String = Defaults.DB_USER,
    dbPassword: String = Defaults.DB_PASSWORD,
    co2Devices: Map[String, Co2Device] = Map.empty,
    timeZone: ZoneId = ZoneId.systemDefault()
)
