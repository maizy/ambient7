package ru.maizy.ambient7.webapp

import ru.maizy.ambient7.webapp.data.Co2Device

case class AppConfig(
    dbUrl: String = "jdbc:h2:file:./analysis;AUTO_SERVER=TRUE",
    dbUser: String = "ambient7",
    dbPassword: String = "",
    co2Devices: Map[String, Co2Device] = Map.empty
)
