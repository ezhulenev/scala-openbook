name := "Scala OpenBook"

version := "0.0.1"

organization := "com.scalafi.openbook"


scalaVersion := "2.11.1"

scalacOptions += "-deprecation"


scalaSource in Compile := baseDirectory.value / "src"

scalaSource in Test := baseDirectory.value / "test"


// Library Dependencies

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"