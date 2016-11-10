enablePlugins(JavaAppPackaging)

name := "ambient7-analysis"


libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % "4.0.3"
)


// scalastyle
(test in Test) := {
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
  (test in Test).value
}
scalastyleFailOnError := true


// DB
flywayUrl := "jdbc:h2:file:./target/analysis;AUTO_SERVER=TRUE"
flywayUser := "ambient7"
