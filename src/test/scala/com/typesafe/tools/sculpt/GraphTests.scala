// Copyright (C) 2015-2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt
import model._

import org.scalatest.FunSuite

object GraphTests {
  // also used by Samples.main
  def toGraphString(name: String, json: String): String = {
    import spray.json._
    import ModelJsonProtocol._
    val dependencies = json.parseJson.convertTo[Seq[FullDependency]]
    val graph = Graph.apply(name, dependencies)
    graph.fullString
  }
}

class GraphTests extends FunSuite {
  for {
    sample <- Samples.samples
  } test(sample.name) {
    assertResult(sample.graph) {
      GraphTests.toGraphString(sample.name, sample.json)
    }
  }
}
