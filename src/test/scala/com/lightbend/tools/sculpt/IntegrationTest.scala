// Copyright (C) 2015-2016 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt

import scala.tools.nsc.{ Settings, Global }
import scala.tools.nsc.io.VirtualDirectory
import scala.reflect.internal.util.BatchSourceFile

object Scaffold {

  val classes: String = {
    val relative = "./target/scala-2.11/classes"
    val file = new java.io.File(relative)
    assert(file.exists)
    file.getAbsolutePath
  }

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

import org.scalatest.FunSuite

class IntegrationTest extends FunSuite {
  def check(s: Sample): Unit = {
    assertResult(s.json) {
      Scaffold.analyze(s.source)
    }
    assertResult(s.classJson) {
      Scaffold.analyze(s.source, classMode = true)
    }
  }
  for (sample <- Samples.samples)
    test(sample.name) {
      check(sample)
    }
}
