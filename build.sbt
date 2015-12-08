organization  := "com.typesafe"
name          := "scala-sculpt"
version       := "0.0.1"
licenses      := Seq("Apache License v2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage      := Some(url("http://github.com/typesafehub/scala-sculpt"))

scalaVersion  := "2.11.7"

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings"
)

pomExtra := (<scm>
  <url>https://github.com/typesafehub/scala-sculpt.git</url>
  <connection>scm:https://github.com/typesafehub/scala-sculpt.git</connection></scm>)
