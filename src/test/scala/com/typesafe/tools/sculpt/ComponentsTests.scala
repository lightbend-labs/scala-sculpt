// Copyright (C) 2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt
import model._

import org.scalatest.FunSuite

object ComponentsTests {
  // also used by Samples.main
  def toCycleString(name: String, json: String): String = {
    import spray.json._
    import ModelJsonProtocol._
    val deps = json.parseJson.convertTo[Seq[FullDependency]]
    val classJson = FullDependenciesPrinter.print(ClassMode(deps).toJson)
    val graph = GraphTests.toGraph(name, classJson)
    Components(graph)
      .filter(_.size > 1)
      .map(_.toSeq.sortBy(_.toString).mkString(" "))
      .sortBy(_.toString)
      .mkString("\n")
  }
}

class ComponentsTests extends FunSuite {
  for {
    sample <- Samples.samples
  } test(sample.name) {
    assertResult(sample.cycles) {
      ComponentsTests.toCycleString(sample.name, sample.json)
    }
  }
}
