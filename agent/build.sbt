enablePlugins(GitVersioning)
enablePlugins(JavaAppPackaging)

name := "ambient7-agent"
organization := "ru.maizy"
scalaVersion := "2.11.7"
git.baseVersion := "0.0.1"
fork in run := true

libraryDependencies ++= Seq(
  "net.java.dev.jna" % "jna" % "4.1.0",
  "com.google.guava" % "guava" % "18.0",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

scalacOptions ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-Xfatal-warnings",
  "-Xlint"
)

// scalastyle
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
lazy val testScalastyleInCompile = taskKey[Unit]("testScalastyleInCompile")
testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
testScalastyleInCompile := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
(test in Test) <<= (test in Test) dependsOn (testScalastyle, testScalastyleInCompile)
scalastyleFailOnError := true
