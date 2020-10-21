// Copyright (C) 2015-2020 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt

import scala.io.Codec

import com.lightbend.tools.sculpt.model.ModelJsonProtocol._
import com.lightbend.tools.sculpt.model._
import spray.json._

/** REPL commands and features */
package object cmd {
  /** Load a Sculpt model JSON file and return the model */
  def loadModel(path: String): Seq[FullDependency] = {
    val s = new scala.reflect.io.File(new java.io.File(path))(Codec.UTF8).slurp()
    s.parseJson.convertTo[Seq[FullDependency]]
  }

  /** Load a Sculpt model JSON file and return the graph.
    * If classMode is true, convert detailed dependencies
    * to aggregated class-level dependencies. */
  def load(path: String, classMode: Boolean = false): Graph = {
    val m0 = loadModel(path)
    val m =
      if (classMode)
        ClassMode(m0)
      else
        m0
    Graph(path, m)
  }

  /** Save a Sculpt model JSON file */
  def saveModel(m: Seq[FullDependency], path: String): Unit =
    new scala.reflect.io.File(new java.io.File(path))(Codec.UTF8).writeAll(FullDependenciesPrinter(m.toJson))

  /** Save a graph to a Sculpt model JSON file */
  def save(graph: Graph, path: String): Unit =
    saveModel(graph.toJsonModel, path)
}
