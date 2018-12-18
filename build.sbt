name := "ambient7"
organization := "ru.maizy"
scalaVersion := "2.11.8"

val scalacOpts = Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-explaintypes",
  // "-Xfatal-warnings",
  "-Xlint:_",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard"
)
lazy val commonSettings = Seq(
  organization := "ru.maizy",
  version := "0.3.1",
  scalaVersion := "2.11.8",
  maintainer := "Nikita Kovaliov <nikita@maizy.ru>",
  packageSummary := "Tools for home climate monitoring",
  scalacOptions ++= scalacOpts ++ Seq("-Ywarn-unused-import"),
  scalacOptions in (Compile, console) := scalacOpts
)

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "com.typesafe" % "config" % "1.3.1",
    "com.github.kxbmap" %% "configs" % "0.4.4",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  )
)

lazy val httpClientDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scalaj" %% "scalaj-http" % "2.3.0", // TODO: iss #39: remove sync methods and this dependancy
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
  )
)

lazy val rdbmsDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc" % "2.5.0",
    "com.h2database" % "h2" % "1.4.193"
  )
)

lazy val cliDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.github.scopt" %% "scopt" % "3.5.0"
  )
)

lazy val jsonDependencies = Seq(
  libraryDependencies ++= Seq(
    "io.spray" %%  "spray-json" % "1.3.2"
  )
)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .settings(jsonDependencies: _*)
  .settings(cliDependencies: _*)
  .dependsOn(influxDbClient)

lazy val rdbmsService = project
  .in(file("rdbms-service"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .settings(rdbmsDependencies: _*)
  .dependsOn(core)

lazy val influxDbClient = project
  .in(file("influxdb-client"))
  .settings(commonSettings: _*)
  .settings(commonDependencies: _*)
  .settings(jsonDependencies: _*)
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
  .settings(jsonDependencies: _*)
  .dependsOn(core)
  .dependsOn(rdbmsService)
  .dependsOn(influxDbClient)
