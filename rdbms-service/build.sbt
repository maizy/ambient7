name := "ambient7-rdbms-service"

// scalastyle
(test in Test) := {
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value
  (test in Test).value
}
scalastyleFailOnError := true
