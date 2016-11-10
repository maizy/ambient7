logLevel := Level.Warn

resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Flyway" at "https://flywaydb.org/repo"
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.0.3")

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.5.1")
addSbtPlugin("com.earldouglas"  % "xsbt-web-plugin" % "2.1.0")
