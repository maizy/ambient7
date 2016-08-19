package ru.maizy.ambient7.webapp

import ru.maizy.ambient7.webapp.data.MT8057Device

case class AppConfig(
    dbUrl: String = "jdbc:h2:file:./analysis;AUTO_SERVER=TRUE",
    dbUser: String = "ambient7",
    dbPassword: String = "",
    mt8057Devices: Map[String, MT8057Device] = Map.empty
)
