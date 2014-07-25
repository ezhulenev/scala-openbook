name := "Scala OpenBook"

version := "0.0.1"

organization := "com.scalafi.openbook"


scalaVersion := "2.10.4"

scalacOptions += "-deprecation"

scalacOptions += "-feature"


// Library Dependencies

libraryDependencies ++= Seq(
  "org.scalaz"    %% "scalaz-core" % "7.0.6"
)

// Test Dependencies

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "2.2.0" % "test"
)