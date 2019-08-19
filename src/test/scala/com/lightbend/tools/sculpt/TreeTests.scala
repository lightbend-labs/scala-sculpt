// Copyright (C) 2015-2019 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt
import model._

object TreeTests extends verify.BasicTestSuite {

  // also used by Samples.main
  def toTreeString(name: String, json: String): String = {
    import spray.json._
    import ModelJsonProtocol._
    val dependencies = json.parseJson.convertTo[Seq[FullDependency]]
    val graph = Graph.apply(name, dependencies)
    TreePrinter(graph)
  }

  for(sample <- Samples.samples)
    test(sample.name) {
      assert(sample.tree ==
        TreeTests.toTreeString(sample.name, sample.json))
    }

}
