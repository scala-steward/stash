name := "stash"
version := "0.1"
scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.slick"         %% "slick"          % "3.3.2",
  "ch.qos.logback"             % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.2",
  "org.scalatest"              %% "scalatest"      % "3.0.8" % Test
)
