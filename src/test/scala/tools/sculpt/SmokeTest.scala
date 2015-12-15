package scala.tools.sculpt

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

import org.junit.Test
import org.junit.Assert.assertEquals

class SmokeTest {
  @Test def generate1(): Unit = {
    val s = Sample.sample1
    assertEquals(s.json,
      Scaffold.analyze(s.source))
  }
  @Test def generate2(): Unit = {
    val s = Sample.sample2
    assertEquals(s.json,
      Scaffold.analyze(s.source))
  }
  @Test def generate3(): Unit = {
    val s = Sample.sample3
    assertEquals(s.json,
      Scaffold.analyze(s.source))
  }
  @Test def generate4(): Unit = {
    val s = Sample.sample4
    assertEquals(s.json,
      Scaffold.analyze(s.source))
  }
  @Test def generate5(): Unit = {
    val s = Sample.sample5
    assertEquals(s.json,
      Scaffold.analyze(s.source))
  }
}
