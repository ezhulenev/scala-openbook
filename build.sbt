import bintray.Keys._

name := "Scala OpenBook"

version := "0.0.8"

organization := "com.scalafi"

licenses in ThisBuild += ("MIT", url("http://opensource.org/licenses/MIT"))

scalaVersion := "2.11.2"

crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.2")

scalacOptions += "-deprecation"

scalacOptions += "-feature"


// Resolvers

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

// Test Dependencies

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "2.2.0" % "test"
)

// Configure publishing to bintray

bintrayPublishSettings

repository in bintray := "releases"

bintrayOrganization in bintray := None
