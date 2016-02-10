// Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt
import model._

import org.scalatest.FunSuite

class TreeTests extends FunSuite {

  def check(sample: Sample): Unit = {
    import spray.json._
    import ModelJsonProtocol._
    val dependencies = sample.json.parseJson.convertTo[Seq[FullDependency]]
    val graph = Graph.apply(sample.name, dependencies)
    assertResult(sample.tree) {
      TreePrinter(graph)
    }
  }

  for(sample <- Sample.samples)
    test(sample.name) {
      check(sample)
    }

}
