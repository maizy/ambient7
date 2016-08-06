enablePlugins(JavaAppPackaging)

name := "ambient7-analysis"


libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "2.4.1",
  "com.h2database" % "h2" % "1.4.192",
  "org.flywaydb" % "flyway-core" % "4.0.3",
  "io.spray" %%  "spray-json" % "1.3.2"
)


// scalastyle
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
lazy val testScalastyleInCompile = taskKey[Unit]("testScalastyleInCompile")
testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
testScalastyleInCompile := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
(test in Test) <<= (test in Test) dependsOn (testScalastyle, testScalastyleInCompile)
scalastyleFailOnError := true


// DB
flywayUrl := "jdbc:h2:file:./target/analysis;AUTO_SERVER=TRUE"
flywayUser := "ambient7"
