package scala.tools.sculpt.plugin

import java.io.File

import scala.tools.nsc
import nsc.Global
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

// after `compile` and `copyResources`,
// use as `scalac -Xplugin:~/git/scala-sculpt/target/scala-2.11/classes -Xplugin-require:sculpt`
class SculptPlugin(val global: Global) extends Plugin {
  val name        = "sculpt"
  val description = "Aid in modularizing big code bases"

  object extractDependencies extends {
    val global = SculptPlugin.this.global
  } with ExtractDependencies {
    val runsAfter = List("refchecks")
    var outputPath: Option[File] = None
  }

  val components = List[PluginComponent](extractDependencies)

  override val optionsHelp: Option[String] = Some(
    "  -P:sculpt:out=<path>            Path to write dependency file to (default: stdout)"
  )

  override def init(options: List[String], error: String => Unit) = {
    implicit class Regex(sc: StringContext) {
      def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
    }
    options.foreach {
      case r"out=(.*)$out" => extractDependencies.outputPath = Some(new File(out))
      case arg => error(s"Bad argument: $arg")
    }
    true
  }
}
