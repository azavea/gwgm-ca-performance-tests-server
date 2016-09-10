name          := "comparative-analysis-bastion"
organization  := "geotrellis"
version       := "0.0.1"
scalaVersion  := "2.11.8"

resolvers ++= Seq(
  "boundless" at "https://repo.boundlessgeo.com/release",
  "boundlessgeo" at "https://boundless.artifactoryonline.com/boundless/main",
  "geomesa releases" at "https://repo.locationtech.org/content/repositories/releases/",
  "geomesa snapshots" at "https://repo.locationtech.org/content/repositories/snapshots/",
  "geosolutions" at "http://maven.geo-solutions.it/",
  "geowave release" at "http://geowave-maven.s3-website-us-east-1.amazonaws.com/release",
  "geowave snapshot" at "http://geowave-maven.s3-website-us-east-1.amazonaws.com/snapshot",
  "osgeo" at "http://download.osgeo.org/webdav/geotools/",
  "sfcurve releases" at "https://repo.locationtech.org/content/repositories/sfcurve-releases",
  Resolver.bintrayRepo("hseeberger", "maven"),
  Resolver.jcenterRepo
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "utf8",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:reflectiveCalls",
  "-unchecked"
)

libraryDependencies ++= {
  val akkaV            = "2.4.8"
  val ficusV           = "1.2.4"
  val circeV           = "0.4.1"
  val scalaTestV       = "3.0.0-M4"
  val scalaMockV       = "3.2.2"
  val scalazScalaTestV = "0.3.0"
  val akkaCirceV       = "1.8.0"
  val geomesaV         = "1.2.6"
  val geowaveV         = "0.9.3-SNAPSHOT"
  val geotoolsV        = "14.3"
  val sfcurveV         = "0.2.0"
  val jaiV             = "1.1.3"
  val accumuloV        = "1.7.1"

  Seq(
    "com.amazonaws" % "aws-java-sdk" % "1.11.31",
    "com.typesafe.akka" %% "akka-http-core"                    % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaV,
    "com.iheart"        %% "ficus"                             % ficusV,
    "io.circe"          %% "circe-core"                        % circeV,
    "io.circe"          %% "circe-generic"                     % circeV,
    "io.circe"          %% "circe-parser"                      % circeV,
    "de.heikoseeberger" %% "akka-http-circe"                   % akkaCirceV,
    "org.geotools"      %  "gt-geotiff"                        % geotoolsV,
    "org.scalatest"     %% "scalatest"                         % scalaTestV  % "test",
    "org.scalamock"     %% "scalamock-scalatest-support"       % scalaMockV  % "test",
    "com.typesafe.akka" %% "akka-http-testkit"                 % akkaV       % "test",
    "org.locationtech.geomesa"  % "geomesa-accumulo-datastore" % geomesaV,
    "javax.media"       %  "jai_core"                          % jaiV
      from "https://s3.amazonaws.com/geowave-geomesa-comparison/jars/jai_core-1.1.3.jar",
    "org.apache.accumulo" % "accumulo-core"                    % accumuloV
      exclude("org.jboss.netty", "netty")
      exclude("org.apache.hadoop", "hadoop-client"),
    "mil.nga.giat" % "geowave-adapter-raster" % geowaveV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "mil.nga.giat" % "geowave-adapter-vector" % geowaveV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "mil.nga.giat" % "geowave-core-store" % geowaveV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
          ExclusionRule(organization = "javax.servlet")),
    "mil.nga.giat" % "geowave-datastore-accumulo" % geowaveV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "org.geoserver" % "gs-wms" % "2.8.2"
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "org.geotools" % "gt-coverage" % geotoolsV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "org.geotools" % "gt-epsg-hsql" % geotoolsV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "org.geotools" % "gt-geotiff" % geotoolsV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "org.geotools" % "gt-main" % geotoolsV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet")),
    "org.geotools" % "gt-referencing" % geotoolsV
      excludeAll(ExclusionRule(organization = "org.mortbay.jetty"),
        ExclusionRule(organization = "javax.servlet"))
  )
}

lazy val root = project.in(file("."))
Revolver.settings

initialCommands := """|import akka.actor._
                      |import akka.pattern._
                      |import akka.util._
                      |import scala.concurrent._""".stripMargin

publishArtifact in Test := false
pomIncludeRepository := { _ => false }

// When creating fat jar, remove some files with
// bad signatures and resolve conflicts by taking the first
// versions of shared packaged types.
assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF\\MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.discard
  case "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
  case "META-INF/BCKEY.SF" => MergeStrategy.discard
  case "META-INF/BCKEY.DSA" => MergeStrategy.discard
  case x if x.startsWith("META-INF/services") => MergeStrategy.concat
  case _ => MergeStrategy.first
}
