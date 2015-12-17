organization  := "com.typesafe"
name          := "scala-sculpt"
version       := "0.1.0"
licenses      := Seq("Apache License v2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage      := Some(url("http://github.com/typesafehub/scala-sculpt"))

scalaVersion  := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  "io.spray" %% "spray-json" % "1.3.2",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

fork in Test := true

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings"
)

mappings in (Compile, packageBin) +=
  (baseDirectory.value / "README.md") -> "README.md"

pomExtra := (<scm>
  <url>https://github.com/typesafehub/scala-sculpt.git</url>
  <connection>scm:https://github.com/typesafehub/scala-sculpt.git</connection></scm>)
