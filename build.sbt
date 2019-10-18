name := "stash"
version := "0.1"
scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "org.postgresql"             % "postgresql"        % "42.2.8",
  "io.monix"                   %% "monix"            % "3.0.0",
  "io.getquill"                %% "quill-jdbc-monix" % "3.4.10",
  "ch.qos.logback"             % "logback-classic"   % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"    % "3.9.2",
  "org.scalatest"              %% "scalatest"        % "3.0.8" % Test
)
