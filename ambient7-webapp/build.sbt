enablePlugins(JettyPlugin)

name := "ambient7-webapp"

val scalatraVersion = "2.4.1"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % scalatraVersion,
  "org.scalatra" %% "scalatra-json" % scalatraVersion,
  "org.json4s" %% "json4s-jackson" % "3.4.0",  // TODO: use spray-json for json
  "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test",
  "org.eclipse.jetty" % "jetty-webapp" % "9.3.11.v20160721" % "container;compile",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
)

// web app settings
containerPort := 22480


// scalastyle
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
lazy val testScalastyleInCompile = taskKey[Unit]("testScalastyleInCompile")
testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
testScalastyleInCompile := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
(test in Test) <<= (test in Test) dependsOn (testScalastyle, testScalastyleInCompile)
scalastyleFailOnError := true
