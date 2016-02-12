// Copyright (C) 2015-2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt
import model._

import org.scalatest.FunSuite

object GraphTests {
  // also used by Samples.main
  def toGraph(name: String, json: String): Graph = {
    import spray.json._
    import ModelJsonProtocol._
    val dependencies = json.parseJson.convertTo[Seq[FullDependency]]
    Graph.apply(name, dependencies)
  }
}

class GraphTests extends FunSuite {
  for {
    sample <- Samples.samples
  } test(sample.name) {
    assertResult(sample.graph) {
      GraphTests.toGraph(sample.name, sample.json).fullString
    }
  }
}
