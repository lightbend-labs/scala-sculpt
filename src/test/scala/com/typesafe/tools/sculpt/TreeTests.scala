// Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt
import model._

import org.scalatest.FunSuite

object TreeTests {
  // also used by Samples.main
  def toTreeString(name: String, json: String): String = {
    import spray.json._
    import ModelJsonProtocol._
    val dependencies = json.parseJson.convertTo[Seq[FullDependency]]
    val graph = Graph.apply(name, dependencies)
    TreePrinter(graph)
  }
}

class TreeTests extends FunSuite {
  for(sample <- Samples.samples)
    test(sample.name) {
      assertResult(sample.tree) {
        TreeTests.toTreeString(sample.name, sample.json)
      }
    }
}
