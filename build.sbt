organization := "com.lightbend"
name := "scala-sculpt"
version := "0.1.4-SNAPSHOT"
licenses := Seq(
  "Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("http://github.com/lightbend/scala-sculpt"))

scalaVersion := crossScalaVersions.value.head
crossScalaVersions := Seq("2.13.8", "2.12.16")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  "io.spray" %% "spray-json" % "1.3.6",
  "org.scalameta" %% "munit" % "0.7.29" % Test,
)
testFrameworks += new TestFramework("munit.Framework")

// so we can run the Scala compiler during integration testing without
// weird problems
Test / fork := true

// so the output of `Test/runMain ...Samples` doesn't get tagged with [info]
Test / outputStrategy := Some(StdoutOutput)

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint",
  "-Xfatal-warnings",
)

// generate same JAR name as `package` would:
// - don't append "-assembly"; see issue #18
// - and do include the "_2.1x", don't know why assembly removes that by default
assembly / assemblyJarName :=
  s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"

assembly / assemblyOption :=
  (assembly / assemblyOption).value.withIncludeScala(false)

Compile / unmanagedResources ++=
  Seq("README.md", "LICENSE")
    .map(baseDirectory.value / _)

pomExtra := (<scm>
  <url>https://github.com/lightbend/scala-sculpt.git</url>
  <connection>scm:https://github.com/lightbend/scala-sculpt.git</connection></scm>)

// configure sbt-header -- to update, run
// `headerCreate` and `test:headerCreate`
headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment)
headerLicense := Some(HeaderLicense.Custom(
  "Copyright (C) 2015-2022 Lightbend Inc. <http://lightbend.com>"))

// scalafix; run with `scalafixEnable` followed by `scalafixAll`
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
