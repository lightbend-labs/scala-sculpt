// Copyright (C) 2015-2021 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt
import model._

object CyclesTests {
  // also used by Samples.main
  def toCyclesAndLayersStrings(name: String, json: String): (String, String) = {
    val graph: Graph = {
      import spray.json._
      import ModelJsonProtocol._
      val deps = json.parseJson.convertTo[Seq[FullDependency]]
      val classJson = FullDependenciesPrinter.print(ClassMode(deps).toJson)
      GraphTests.toGraph(name, classJson)
    }
    (Cycles.cyclesString(graph.nodes),
     Cycles.layersString(graph.nodes))
  }
}

class CyclesTests extends munit.FunSuite {
  for (sample <- Samples.samples)
    test(sample.name) {
      val (cycles, layers) = CyclesTests.toCyclesAndLayersStrings(sample.name, sample.json)
      assert(sample.cycles == cycles)
      assert(sample.layers == layers)
    }
}
