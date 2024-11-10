ThisBuild / version := "1.0.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "Spotify"
  )


val sparkVersion = "3.5.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion
)

libraryDependencies += "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M6"
libraryDependencies += "com.lihaoyi" %% "upickle" % "1.4.2"