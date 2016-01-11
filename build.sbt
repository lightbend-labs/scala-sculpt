// Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>

organization  := "com.typesafe"
name          := "scala-sculpt"
version       := "0.1.1"
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

// generate same JAR name as `package` would:
// - don't append "-assembly"; see issue #18
// - and do include the "_2.11", don't know why assembly removes that by default
assemblyJarName in assembly :=
  s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"

mappings in (Compile, packageBin) ++= Seq(
  (baseDirectory.value / "README.md") -> "README.md",
  (baseDirectory.value / "LICENSE.md") -> "LICENSE.md")

pomExtra := (<scm>
  <url>https://github.com/typesafehub/scala-sculpt.git</url>
  <connection>scm:https://github.com/typesafehub/scala-sculpt.git</connection></scm>)
