organization  := "com.lightbend"
name          := "scala-sculpt"
version       := "0.1.4-SNAPSHOT"
licenses      := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage      := Some(url("http://github.com/lightbend/scala-sculpt"))

scalaVersion  := crossScalaVersions.value.head
crossScalaVersions := Seq("2.12.4", "2.11.12")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  "io.spray" %% "spray-json" % "1.3.2",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

// so we can run the Scala compiler during integration testing without
// weird problems
fork in Test := true

// so the output of `test:runMain ...Samples` doesn't get tagged with [info]
outputStrategy in Test := Some(StdoutOutput)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings"
)

// generate same JAR name as `package` would:
// - don't append "-assembly"; see issue #18
// - and do include the "_2.1x", don't know why assembly removes that by default
assemblyJarName in assembly :=
  s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"

assemblyOption in assembly :=
  (assemblyOption in assembly).value.copy(includeScala = false)

unmanagedResources in Compile ++=
  Seq("README.md", "LICENSE")
    .map(baseDirectory.value / _)

pomExtra := (<scm>
  <url>https://github.com/lightbend/scala-sculpt.git</url>
  <connection>scm:https://github.com/lightbend/scala-sculpt.git</connection></scm>)

// configure sbt-header -- to update, run
// `headerCreate` and `test:headerCreate`
headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment)
headerLicense := Some(HeaderLicense.Custom(
  "Copyright (C) 2015-2018 Lightbend Inc. <http://lightbend.com>"))
