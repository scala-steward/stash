enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

name := "stash"
version := "0.1"
scalaVersion := "2.13.1"

// format: off
scalacOptions ++= Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
)
// format: on

libraryDependencies ++= Seq(
  "org.postgresql"             % "postgresql"            % "42.2.9",
  "io.monix"                   %% "monix"                % "3.1.0",
  "io.getquill"                %% "quill-jdbc-monix"     % "3.5.0",
  "com.typesafe.akka"          %% "akka-http"            % "10.1.11",
  "com.typesafe.akka"          %% "akka-http-spray-json" % "10.1.11",
  "com.typesafe.akka"          %% "akka-stream"          % "2.6.1",
  "com.auth0"                  % "java-jwt"              % "3.8.3",
  "com.iheart"                 %% "ficus"                % "1.4.7",
  "ch.qos.logback"             % "logback-classic"       % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.2",
  "org.scalatest"              %% "scalatest"            % "3.1.0" % Test,
  "com.dimafeng"               %% "testcontainers-scala" % "0.34.1" % Test,
  "org.testcontainers"         % "postgresql"            % "1.12.4" % Test,
  "com.typesafe.akka"          %% "akka-stream-testkit"  % "2.6.1" % Test,
  "com.typesafe.akka"          %% "akka-http-testkit"    % "10.1.11" % Test
)

// full output in test exceptions
testOptions in Test += Tests.Argument("-oF")

mainClass := Some("me.herzrasen.stash.Stash")

maintainer in Docker := "dennis.mellert@gmail.com"

val port = 8080

dockerBaseImage := "openjdk:11-jre-slim"
dockerUsername := Some("stash")
daemonUser in Docker := "stash"
dockerExposedPorts ++= Seq(port)
