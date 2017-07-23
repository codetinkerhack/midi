import Dependencies.{scalaTest, _}
import sbt.Keys.libraryDependencies

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.codetinkerhack",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "MIDI",
    libraryDependencies += scalaTest % Test,
    libraryDependencies +=  "org.scream3r" % "jssc" %  "2.8.0"
  )
