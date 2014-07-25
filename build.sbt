name := "Scala OpenBook"

version := "0.0.1"

organization := "com.scalafi.openbook"


scalaVersion := "2.10.4"

scalacOptions += "-deprecation"

scalacOptions += "-feature"


// Resolvers

resolvers += "Pellucid Bintray" at "http://dl.bintray.com/pellucid/maven"

// Library Dependencies

libraryDependencies ++= Seq(
  "org.scalaz"        %% "scalaz-core"   % "7.0.6",
  "org.scalaz.stream" %% "scalaz-stream" % "0.4.1",
  "com.pellucid"      %% "framian"       % "0.1.1"
)

// Test Dependencies

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "2.2.0" % "test"
)