// Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt

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

  val settings: Settings = {
    val settings = new Settings
    settings.processArgumentString(
      s"-usejavacp -Xplugin:$classes -Xplugin-require:sculpt")
    settings.outputDirs.setSingleOutput(
      new VirtualDirectory("(memory)", None))
    settings
  }

  def analyze(code: String): String = {
    val out = java.io.File.createTempFile("sculpt", "json", null)
    settings.processArgumentString(s"-P:sculpt:out=$out")
    val sources = List(new BatchSourceFile("<test>", code))
    val compiler = new Global(settings)
    (new compiler.Run).compileSources(sources)
    scala.io.Source.fromFile(out).mkString
  }

}

import org.scalatest.FunSuite

class SmokeTest extends FunSuite {
  def check(s: Sample): Unit = {
    assertResult(s.json) {
      Scaffold.analyze(s.source)
    }
  }
  for (sample <- Samples.samples)
    test(sample.name) {
      check(sample)
    }
}
