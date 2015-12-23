enablePlugins(GitVersioning)
enablePlugins(JavaAppPackaging)

name := "ambient7-mt8057-agent"
organization := "ru.maizy"
scalaVersion := "2.11.7"

git.baseVersion := "0.0.3"
git.useGitDescribe := true
git.gitTagToVersionNumber := { tag: String =>
  val mask = "([0-9\\.]+)".r
  tag match {
    case mask(v) => Some(v)
    case _ => None
  }
}

fork in run := true
outputStrategy := Some(StdoutOutput)

libraryDependencies ++= Seq(
  "net.java.dev.jna" % "jna" % "4.1.0",
  "com.google.guava" % "guava" % "18.0",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "org.scalaj" %% "scalaj-http" % "2.2.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
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
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
lazy val testScalastyleInCompile = taskKey[Unit]("testScalastyleInCompile")
testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
testScalastyleInCompile := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
(test in Test) <<= (test in Test) dependsOn (testScalastyle, testScalastyleInCompile)
scalastyleFailOnError := true
