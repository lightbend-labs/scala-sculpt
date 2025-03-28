// Copyright (C) Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt

import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.{Global, Settings}

object Scaffold {

  val classes: String =
    new java.io.File("./target/scala-2.13/classes")
      .ensuring(_.exists)
      .getAbsolutePath

  def defaultSettings: Settings = {
    val settings = new Settings
    settings.processArgumentString(
      s"-usejavacp -Xplugin:$classes -Xplugin-require:sculpt")
    settings.outputDirs.setSingleOutput(
      new VirtualDirectory("(memory)", None))
    settings
  }

  def analyze(code: String, classMode: Boolean = false): String = {
    val out = java.io.File.createTempFile("sculpt", "json", null)
    val modeSetting =
      if (classMode)
        " -P:sculpt:mode=class"
      else
        ""
    val settings = defaultSettings
    settings.processArgumentString(s"-P:sculpt:out=$out$modeSetting")
    val sources = List(new BatchSourceFile("<test>", code))
    val compiler = new Global(settings)
    (new compiler.Run).compileSources(sources)
    scala.io.Source.fromFile(out).mkString
  }

}

class IntegrationTest extends munit.FunSuite {
  def check(s: Sample): Unit = {
    assert(s.json == Scaffold.analyze(s.source))
    assert(s.classJson == Scaffold.analyze(s.source, classMode = true))
  }
  for (sample <- Samples.samples)
    test(sample.name) {
      check(sample)
    }
}
