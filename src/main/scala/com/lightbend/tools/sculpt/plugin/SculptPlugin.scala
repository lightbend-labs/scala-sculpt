// Copyright (C) 2015-2021 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.plugin

import com.lightbend.tools.sculpt.util.RegexInterpolator

import java.io.File
import scala.tools.nsc

import nsc.Global
import nsc.plugins.{ Plugin, PluginComponent }

class SculptPlugin(val global: Global) extends Plugin {
  val name        = "sculpt"
  val description = "Aid in modularizing big codebases"

  object extractDependencies extends ExtractDependencies {
    val global = SculptPlugin.this.global
    val runsAfter = List("refchecks")
    var outputPath: Option[File] = None
    var classMode = false
  }

  val components = List[PluginComponent](extractDependencies)

  override val optionsHelp: Option[String] = Some(
    "  -P:sculpt:out=<path>     Path to write dependency file to (default: stdout)\n" +
    "  -P:sculpt:mode=class     Run in 'class mode' instead of default fine-grained mode"
  )

  override def init(options: List[String], error: String => Unit) = {
    options.foreach {
      case r"out=(.*)$out" =>
        extractDependencies.outputPath = Some(new File(out))
      case r"mode=class" =>
        extractDependencies.classMode = true
      case arg =>
        error(s"Bad argument: $arg")
    }
    true
  }
}
