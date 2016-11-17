enablePlugins(JettyPlugin)

name := "ambient7-webapp"

val scalatraVersion = "2.4.1"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % scalatraVersion,
  "org.scalatra" %% "scalatra-json" % scalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test",
  "org.eclipse.jetty" % "jetty-webapp" % "9.3.11.v20160721" % "container;compile",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "org.json4s" %% "json4s-native" % "3.5.0" % "test"
)

// web app settings
containerPort := 22480


// scalastyle
(test in Test) := {
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
  (test in Test).value
}
scalastyleFailOnError := true
