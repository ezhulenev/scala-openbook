import bintray.Keys._

name := "Scala OpenBook"

version := "0.0.3"

organization := "com.scalafi"

licenses in ThisBuild += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.11.2"

crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.2")

scalacOptions += "-deprecation"

scalacOptions += "-feature"


// Resolvers

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

// Library Dependencies

libraryDependencies ++= Seq(
  "org.scalaz"        %% "scalaz-core"   % "7.1.0",
  "org.scalaz.stream" %% "scalaz-stream" % "0.5a"
)

// Test Dependencies

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "2.2.0" % "test"
)

// Configure publishing to bintray

bintrayPublishSettings

repository in bintray := "releases"

bintrayOrganization in bintray := None
