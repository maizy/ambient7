enablePlugins(GitVersioning)
enablePlugins(JavaAppPackaging)

name := "ambient7-analysis"
organization := "ru.maizy"
scalaVersion := "2.11.8"

git.baseVersion := "0.1.0"
git.useGitDescribe := true
git.gitTagToVersionNumber := { tag: String =>
  val mask = "([0-9\\.]+)".r
  tag match {
    case mask(v) => Some(v)
    case _ => None
  }
}

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.scalikejdbc" %% "scalikejdbc" % "2.4.1",
  "com.h2database" % "h2" % "1.4.192",
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


// DB
flywayUrl := "jdbc:h2:file:./target/analysis;AUTO_SERVER=TRUE"
flywayUser := "ambient7"
