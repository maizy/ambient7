name := "ambient7"
organization := "ru.maizy"
scalaVersion := "2.11.8"

lazy val commonSettings = Seq(
  organization := "ru.maizy",
  version := "0.2.0",
  scalaVersion := "2.11.8",
  maintainer := "Nikita Kovaliov <nikita@maizy.ru>",
  packageSummary := "Tools for home climate monitoring",
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-deprecation",
    "-unchecked",
    "-explaintypes",
    "-Xfatal-warnings",
    "-Xlint:_",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import"
  )
)

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "com.typesafe" % "config" % "1.3.0",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  )
)

lazy val httpClientDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scalaj" %% "scalaj-http" % "2.3.0"
  )
)

lazy val rdbmsDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc" % "2.4.1",
    "com.h2database" % "h2" % "1.4.192"
  )
)

lazy val cliDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.github.scopt" %% "scopt" % "3.5.0"
  )
)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .dependsOn(influxDbClient)

lazy val influxDbClient = project
  .in(file("influxdb-client"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .settings(httpClientDependencies: _*)

lazy val mt8057Agent = project
  .in(file("mt8057-agent"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .settings(cliDependencies: _*)
  .dependsOn(core)
  .dependsOn(influxDbClient)

lazy val ambient7Analysis = project
  .in(file("ambient7-analysis"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .settings(rdbmsDependencies: _*)
  .settings(cliDependencies: _*)
  .dependsOn(core)
  .dependsOn(influxDbClient)

lazy val ambient7WebApp = project
  .in(file("ambient7-webapp"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .settings(rdbmsDependencies: _*)
  .dependsOn(core)
