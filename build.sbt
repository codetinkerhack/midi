lazy val root = (project in file("."))
  .settings(
    name := "MIDI",
    organization := "com.codetinkerhack",
    scalaVersion := "2.12.10",
    version      := "0.1.0-SNAPSHOT",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    libraryDependencies +=  "org.scream3r" % "jssc" %  "2.8.0"
  )
