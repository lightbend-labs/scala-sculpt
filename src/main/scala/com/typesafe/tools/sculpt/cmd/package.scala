// Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt

import spray.json._
import scala.io.Codec
import com.typesafe.tools.sculpt.model._
import com.typesafe.tools.sculpt.model.ModelJsonProtocol._

/** REPL commands and features */
package object cmd {
  /** Load a Sculpt model JSON file and return the model */
  def loadModel(path: String): Seq[FullDependency] = {
    val s = new scala.reflect.io.File(new java.io.File(path))(Codec.UTF8).slurp()
    s.parseJson.convertTo[Seq[FullDependency]]
  }

  /** Load a Sculpt model JSON file and return the graph */
  def load(path: String): Graph = {
    val m = loadModel(path)
    Graph(path, m)
  }

  /** Save a Sculpt model JSON file */
  def saveModel(m: Seq[FullDependency], path: String): Unit =
    new scala.reflect.io.File(new java.io.File(path))(Codec.UTF8).writeAll(FullDependenciesPrinter(m.toJson))

  /** Save a graph to a Sculpt model JSON file */
  def save(graph: Graph, path: String): Unit =
    saveModel(graph.toJsonModel, path)
}
