lazy val root = (project in file("."))
  .settings(
    name := "scala-midi",
    organization := "com.codetinkerhack",
    scalaVersion := "2.12.7",
    version      := "0.1.0-SNAPSHOT",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test",
    libraryDependencies +=  "org.scream3r" % "jssc" %  "2.8.0"
  )
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"