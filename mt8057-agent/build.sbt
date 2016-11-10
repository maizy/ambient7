enablePlugins(JavaAppPackaging)

name := "ambient7-mt8057-agent"

fork in run := true
outputStrategy := Some(StdoutOutput)

libraryDependencies ++= Seq(
  "net.java.dev.jna" % "jna" % "4.1.0",
  "com.google.guava" % "guava" % "20.0"
)

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-Xfatal-warnings",
  "-Xlint"
)

// scalastyle
(test in Test) := {
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
  (test in Test).value
}
scalastyleFailOnError := true
