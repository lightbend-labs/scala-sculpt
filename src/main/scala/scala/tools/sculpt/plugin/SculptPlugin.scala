package scala.tools.sculpt.plugin

import scala.tools.nsc
import nsc.Global
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent

// after `compile` and `copyResources`,
// use as `scalac -Xplugin:~/git/scala-sculpt/target/scala-2.11/classes -Xplugin-require:scala-sculpt`
class SculptPlugin(val global: Global) extends Plugin {
  val name        = "scala-sculpt"
  val description = "Aid in modularizing big code bases"

  object extractDependencies extends {
    val global = SculptPlugin.this.global
  } with ExtractDependencies {
    val runsAfter = List("refchecks")
  }

  val components = List[PluginComponent](extractDependencies)
}
