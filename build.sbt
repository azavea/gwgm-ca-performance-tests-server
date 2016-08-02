name          := "comparative-analysis-bastion"
organization  := "geotrellis"
version       := "0.0.1"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.jcenterRepo

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= {
  val akkaV            = "2.4.8"
  val ficusV           = "1.2.4"
  val circeV           = "0.4.1"
  val scalaTestV       = "3.0.0-M4"
  val scalaMockV       = "3.2.2"
  val scalazScalaTestV = "0.3.0"
  val akkaCirceV       = "1.8.0"
  Seq(
    "com.typesafe.akka" %% "akka-http-core"                    % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaV,
    "com.iheart"        %% "ficus"                             % ficusV,
    "io.circe"          %% "circe-core"                        % circeV,
    "io.circe"          %% "circe-generic"                     % circeV,
    "io.circe"          %% "circe-parser"                      % circeV,
    "de.heikoseeberger" %% "akka-http-circe"                   % akkaCirceV,
    "org.scalatest"     %% "scalatest"                         % scalaTestV  % "test",
    "org.scalamock"     %% "scalamock-scalatest-support"       % scalaMockV  % "test",
    "com.typesafe.akka" %% "akka-http-testkit"                 % akkaV       % "test"
  )
}

lazy val root = project.in(file("."))
Defaults.itSettings
Revolver.settings

initialCommands := """|import akka.actor._
                      |import akka.pattern._
                      |import akka.util._
                      |import scala.concurrent._
                      |import scala.concurrent.duration._""".stripMargin

publishArtifact in Test := false
pomIncludeRepository := { _ => false }

