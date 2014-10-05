import com.typesafe.sbt.SbtStartScript

name := "AkkaBehaviorCheck"

version := "0.0.1"

scalaVersion := "2.11.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.3.6"

seq(SbtStartScript.startScriptForClassesSettings: _*)

scalariformSettings
