// Copyright (C) 2015-2018 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt
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

  // test reading JSON and generating a human-readable dump of
  // the resulting Graph object
  for {
    sample <- Samples.samples
  } test(sample.name) {
    assertResult(sample.graph) {
      GraphTests.toGraph(sample.name, sample.json).fullString
    }
  }

  // test `removePaths` as demonstrated in the readme
  test("readme removePaths") {
    val graph = {
      val sample = Samples.samples.find(_.name == "readme").get
      GraphTests.toGraph(sample.name, sample.json)
    }
    assert(graph.fullString.contains("pkt:java.pkt:lang"))
    assert(graph.fullString.contains("Dep2"))
    assertResult((15, 19))((graph.nodes.size, graph.edges.size))
    graph.removePaths("Dep2", "java.lang")
    val expected =
      """|Graph 'readme': 8 nodes, 8 edges
         |Nodes:
         |  - o:Dep1
         |  - pkt:scala.tp:AnyRef
         |  - o:Dep1.def:<init>
         |  - o:Dep1.def:x
         |  - o:Dep1.t:x
         |  - pkt:scala.cl:Int
         |  - o:Dep1.def:y
         |  - o:Dep1.t:y
         |Edges:
         |  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:Dep1.def:<init> -[Uses]-> o:Dep1
         |  - o:Dep1.def:x -[Uses]-> o:Dep1.t:x
         |  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep1.def:y -[Uses]-> o:Dep1.t:y
         |  - o:Dep1.def:y -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep1.t:y -[Uses]-> pkt:scala.cl:Int""".stripMargin
    assertResult(expected) { graph.fullString }
  }

}
