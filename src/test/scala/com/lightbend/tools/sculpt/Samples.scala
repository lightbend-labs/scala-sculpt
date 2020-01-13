// Copyright (C) 2015-2020 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt

import model._

// to add a new sample (or update an existing one):
//   test:runMain com.lightbend.tools.sculpt.Samples name <source code>
// and paste the results below

class Sample(
  val name: String,
  val source: String,
  val json: String,
  val classJson: String,
  val graph: String,
  val tree: String,
  val cycles: String = "",
  val layers: String = ""
) {
  Samples.samples += this
}

object Sample {
  def apply(
             name: String,
             source: String,
             json: String,
             classJson: String,
             graph: String,
             tree: String,
             cycles: String = "",
             layers: String = ""
           ): Sample =
    new Sample(
      name,
      source.replaceAll("\\r\\n", "\n"),
      json.replaceAll("\\r\\n", "\n"),
      classJson.replaceAll("\\r\\n", "\n"),
      graph.replaceAll("\\r\\n", "\n"),
      tree.replaceAll("\\r\\n", "\n"),
      cycles.replaceAll("\\r\\n", "\n"),
      layers.replaceAll("\\r\\n", "\n")
    )
}

object Samples {

  def main(args: Array[String]): Unit = {
    import spray.json._
    import ModelJsonProtocol._
    val (name, source) = (args.head, args.tail.mkString(" "))
    val json = Scaffold.analyze(source)
    val classJson = {
      val deps = json.parseJson.convertTo[Seq[FullDependency]]
      FullDependenciesPrinter.print(ClassMode(deps).toJson)
    }
    val graph = GraphTests.toGraph(name, json).fullString
    val tree = TreeTests.toTreeString(name, json) + "\n"
    val (cycles, layers) = CyclesTests.toCyclesAndLayersStrings(name, classJson)
    def triple(s: String): String =
      s.linesIterator.mkString("\"\"\"|", "\n         |", "\"\"\".stripMargin")
    println(
      s"""@  Sample(
          @    name = "$name",
          @    source =
          @      ${triple(source)},
          @    json =
          @      ${triple(json)},
          @    classJson =
          @      ${triple(classJson)},
          @    graph =
          @      ${triple(graph)},
          @    tree =
          @      ${triple(tree)},
          @    cycles =
          @      ${triple(cycles)},
          @    layers =
          @      ${triple(layers.trim + "\n\n")})""".stripMargin('@'))
  }

  val samples = collection.mutable.Buffer.empty[Sample]

  // test:runMain com.lightbend.tools.sculpt.Samples "lone object" "object O"
  Sample(
    name = "lone object",
    source =
      """|object O""".stripMargin,
    json =
      """|[
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["o:O"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["o:O"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["o:O"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["o:O"], "uses": ["pkt:scala", "tp:AnyRef"]}
         |]""".stripMargin,
    graph =
      """|Graph 'lone object': 4 nodes, 3 edges
         |Nodes:
         |  - o:O
         |  - pkt:scala.tp:AnyRef
         |  - o:O.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |Edges:
         |  - o:O -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:O.def:<init> -[Uses]-> o:O
         |  - o:O.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>""".stripMargin,
    tree =
      """|lone object:
         |└── O
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|""".stripMargin,
    layers =
      """|[1] o:O
         |[0] cl:java.lang.Object
         |[0] tp:scala.AnyRef
         |""".stripMargin)

  // test:runMain com.lightbend.tools.sculpt.Samples "two subclasses" "trait T; class C1 extends T; class C2 extends T"
  Sample(
    name = "two subclasses",
    source =
      """|trait T; class C1 extends T; class C2 extends T""".stripMargin,
    json =
      """|[
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["cl:C1"]},
         |  {"extends": ["tr:T"], "sym": ["cl:C1"]},
         |  {"sym": ["cl:C1", "def:<init>"], "uses": ["cl:C1"]},
         |  {"sym": ["cl:C1", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["cl:C2"]},
         |  {"extends": ["tr:T"], "sym": ["cl:C2"]},
         |  {"sym": ["cl:C2", "def:<init>"], "uses": ["cl:C2"]},
         |  {"sym": ["cl:C2", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["tr:T"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["cl:C1"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["cl:C1"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["cl:C1"], "uses": ["tr:T"]},
         |  {"sym": ["cl:C2"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["cl:C2"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["cl:C2"], "uses": ["tr:T"]},
         |  {"sym": ["tr:T"], "uses": ["pkt:scala", "tp:AnyRef"]}
         |]""".stripMargin,
    graph =
      """|Graph 'two subclasses': 7 nodes, 9 edges
         |Nodes:
         |  - cl:C1
         |  - pkt:scala.tp:AnyRef
         |  - tr:T
         |  - cl:C1.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |  - cl:C2
         |  - cl:C2.def:<init>
         |Edges:
         |  - cl:C1 -[Extends]-> pkt:scala.tp:AnyRef
         |  - cl:C1 -[Extends]-> tr:T
         |  - cl:C1.def:<init> -[Uses]-> cl:C1
         |  - cl:C1.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - cl:C2 -[Extends]-> pkt:scala.tp:AnyRef
         |  - cl:C2 -[Extends]-> tr:T
         |  - cl:C2.def:<init> -[Uses]-> cl:C2
         |  - cl:C2.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - tr:T -[Extends]-> pkt:scala.tp:AnyRef""".stripMargin,
    tree =
      """|two subclasses:
         |└── C1
         |    └── scala.AnyRef
         |    └── T
         |        └── scala.AnyRef
         |└── scala.AnyRef
         |└── T
         |    └── scala.AnyRef
         |└── C2
         |    └── scala.AnyRef
         |    └── T
         |        └── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|""".stripMargin,
    layers =
      """|[2] cl:C1
         |[2] cl:C2
         |[1] tr:T
         |[0] cl:java.lang.Object
         |[0] tp:scala.AnyRef
         |""".stripMargin)

  // test:runMain com.lightbend.tools.sculpt.Samples "circular dependency" "trait T1 { def x: T2 }; trait T2 { def x: T1 }"
  Sample(
    name = "circular dependency",
    source =
      """|trait T1 { def x: T2 }; trait T2 { def x: T1 }""".stripMargin,
    json =
      """|[
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["tr:T1"]},
         |  {"sym": ["tr:T1", "def:x"], "uses": ["tr:T2"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["tr:T2"]},
         |  {"sym": ["tr:T2", "def:x"], "uses": ["tr:T1"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["tr:T1"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["tr:T1"], "uses": ["tr:T2"]},
         |  {"sym": ["tr:T2"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["tr:T2"], "uses": ["tr:T1"]}
         |]""".stripMargin,
    graph =
      """|Graph 'circular dependency': 5 nodes, 4 edges
         |Nodes:
         |  - tr:T1
         |  - pkt:scala.tp:AnyRef
         |  - tr:T1.def:x
         |  - tr:T2
         |  - tr:T2.def:x
         |Edges:
         |  - tr:T1 -[Extends]-> pkt:scala.tp:AnyRef
         |  - tr:T1.def:x -[Uses]-> tr:T2
         |  - tr:T2 -[Extends]-> pkt:scala.tp:AnyRef
         |  - tr:T2.def:x -[Uses]-> tr:T1""".stripMargin,
    tree =
      """|circular dependency:
         |└── T1
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── T2
         |    └── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|[2] tr:T1 tr:T2""".stripMargin,
    layers =
      """|[1] tr:T1 tr:T2
         |[0] tp:scala.AnyRef
         |""".stripMargin)

  // test:runMain com.lightbend.tools.sculpt.Samples "3-cycle" "trait T1 { def t: T2 }; trait T2 { def t: T3 }; trait T3 { def t: T1 }"
  Sample(
    name = "3-cycle",
    source =
      """|trait T1 { def t: T2 }; trait T2 { def t: T3 }; trait T3 { def t: T1 }""".stripMargin,
    json =
      """|[
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["tr:T1"]},
         |  {"sym": ["tr:T1", "def:t"], "uses": ["tr:T2"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["tr:T2"]},
         |  {"sym": ["tr:T2", "def:t"], "uses": ["tr:T3"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["tr:T3"]},
         |  {"sym": ["tr:T3", "def:t"], "uses": ["tr:T1"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["tr:T1"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["tr:T1"], "uses": ["tr:T2"]},
         |  {"sym": ["tr:T2"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["tr:T2"], "uses": ["tr:T3"]},
         |  {"sym": ["tr:T3"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["tr:T3"], "uses": ["tr:T1"]}
         |]""".stripMargin,
    graph =
      """|Graph '3-cycle': 7 nodes, 6 edges
         |Nodes:
         |  - tr:T1
         |  - pkt:scala.tp:AnyRef
         |  - tr:T1.def:t
         |  - tr:T2
         |  - tr:T2.def:t
         |  - tr:T3
         |  - tr:T3.def:t
         |Edges:
         |  - tr:T1 -[Extends]-> pkt:scala.tp:AnyRef
         |  - tr:T1.def:t -[Uses]-> tr:T2
         |  - tr:T2 -[Extends]-> pkt:scala.tp:AnyRef
         |  - tr:T2.def:t -[Uses]-> tr:T3
         |  - tr:T3 -[Extends]-> pkt:scala.tp:AnyRef
         |  - tr:T3.def:t -[Uses]-> tr:T1""".stripMargin,
    tree =
      """|3-cycle:
         |└── T1
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── T2
         |    └── scala.AnyRef
         |└── T3
         |    └── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|[3] tr:T1 tr:T2 tr:T3""".stripMargin,
    layers =
      """|[1] tr:T1 tr:T2 tr:T3
         |[0] tp:scala.AnyRef
         |""".stripMargin)

  // test:runMain com.lightbend.tools.sculpt.Samples "package" "package a.b { class C1; class C2 }"
  Sample(
    name = "package",
    source =
      """|package a.b { class C1; class C2 }""".stripMargin,
    json =
      """|[
         |  {"sym": [], "uses": ["pk:a"]},
         |  {"sym": [], "uses": ["pkt:a", "pk:b"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["pkt:a", "pkt:b", "cl:C1"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C1", "def:<init>"], "uses": ["pkt:a", "pkt:b", "cl:C1"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C1", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["pkt:a", "pkt:b", "cl:C2"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C2", "def:<init>"], "uses": ["pkt:a", "pkt:b", "cl:C2"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C2", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["pkt:a", "pkt:b", "cl:C1"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C1"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C2"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C2"], "uses": ["pkt:scala", "tp:AnyRef"]}
         |]""".stripMargin,
    graph =
      """|Graph 'package': 9 nodes, 8 edges
         |Nodes:
         |  - 
         |  - pk:a
         |  - pkt:a.pk:b
         |  - pkt:a.pkt:b.cl:C1
         |  - pkt:scala.tp:AnyRef
         |  - pkt:a.pkt:b.cl:C1.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |  - pkt:a.pkt:b.cl:C2
         |  - pkt:a.pkt:b.cl:C2.def:<init>
         |Edges:
         |  -  -[Uses]-> pk:a
         |  -  -[Uses]-> pkt:a.pk:b
         |  - pkt:a.pkt:b.cl:C1 -[Extends]-> pkt:scala.tp:AnyRef
         |  - pkt:a.pkt:b.cl:C1.def:<init> -[Uses]-> pkt:a.pkt:b.cl:C1
         |  - pkt:a.pkt:b.cl:C1.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - pkt:a.pkt:b.cl:C2 -[Extends]-> pkt:scala.tp:AnyRef
         |  - pkt:a.pkt:b.cl:C2.def:<init> -[Uses]-> pkt:a.pkt:b.cl:C2
         |  - pkt:a.pkt:b.cl:C2.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>""".stripMargin,
    tree =
      """|package:
         |└── a.b.C1
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── a.b.C2
         |    └── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|""".stripMargin,
    layers =
      """|[1] cl:a.b.C1
         |[1] cl:a.b.C2
         |[0] cl:java.lang.Object
         |[0] tp:scala.AnyRef
         |""".stripMargin)

  // test:runMain com.lightbend.tools.sculpt.Samples "nested class" "trait T; class C { class D extends T }"
  Sample(
    name = "nested class",
    source =
      """|trait T; class C { class D extends T }""".stripMargin,
    json =
      """|[
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["cl:C"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["cl:C", "cl:D"]},
         |  {"extends": ["tr:T"], "sym": ["cl:C", "cl:D"]},
         |  {"sym": ["cl:C", "cl:D", "def:<init>"], "uses": ["cl:C"]},
         |  {"sym": ["cl:C", "cl:D", "def:<init>"], "uses": ["cl:C", "cl:D"]},
         |  {"sym": ["cl:C", "cl:D", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["cl:C", "def:<init>"], "uses": ["cl:C"]},
         |  {"sym": ["cl:C", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["tr:T"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["cl:C"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["cl:C"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["cl:C"], "uses": ["tr:T"]},
         |  {"sym": ["tr:T"], "uses": ["pkt:scala", "tp:AnyRef"]}
         |]""".stripMargin,
    graph =
      """|Graph 'nested class': 7 nodes, 9 edges
         |Nodes:
         |  - cl:C
         |  - pkt:scala.tp:AnyRef
         |  - cl:C.cl:D
         |  - tr:T
         |  - cl:C.cl:D.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |  - cl:C.def:<init>
         |Edges:
         |  - cl:C -[Extends]-> pkt:scala.tp:AnyRef
         |  - cl:C.cl:D -[Extends]-> pkt:scala.tp:AnyRef
         |  - cl:C.cl:D -[Extends]-> tr:T
         |  - cl:C.cl:D.def:<init> -[Uses]-> cl:C
         |  - cl:C.cl:D.def:<init> -[Uses]-> cl:C.cl:D
         |  - cl:C.cl:D.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - cl:C.def:<init> -[Uses]-> cl:C
         |  - cl:C.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - tr:T -[Extends]-> pkt:scala.tp:AnyRef""".stripMargin,
    tree =
      """|nested class:
         |└── C
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── C.D
         |    └── scala.AnyRef
         |    └── T
         |        └── scala.AnyRef
         |└── T
         |    └── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|""".stripMargin,
    layers =
      """|[2] cl:C
         |[1] tr:T
         |[0] cl:java.lang.Object
         |[0] tp:scala.AnyRef
         |""".stripMargin)

  // test:runMain com.lightbend.tools.sculpt.Samples "uses module" "object O { None }"
  Sample(
    name = "uses module",
    source =
      """|object O { None }""".stripMargin,
    json =
      """|[
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["o:O"]},
         |  {"sym": ["o:O"], "uses": ["pk:scala"]},
         |  {"sym": ["o:O"], "uses": ["pkt:scala", "ov:None"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["o:O"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["o:O"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["o:O"], "uses": ["pkt:scala", "o:None"]},
         |  {"sym": ["o:O"], "uses": ["pkt:scala", "tp:AnyRef"]}
         |]""".stripMargin,
    graph =
      """|Graph 'uses module': 6 nodes, 5 edges
         |Nodes:
         |  - o:O
         |  - pkt:scala.tp:AnyRef
         |  - pk:scala
         |  - pkt:scala.ov:None
         |  - o:O.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |Edges:
         |  - o:O -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:O -[Uses]-> pk:scala
         |  - o:O -[Uses]-> pkt:scala.ov:None
         |  - o:O.def:<init> -[Uses]-> o:O
         |  - o:O.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>""".stripMargin,
    tree =
      """|uses module:
         |└── O
         |    └── scala.AnyRef
         |    └── scala
         |    └── scala.None
         |└── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|""".stripMargin,
    layers =
      """|[1] o:O
         |[0] cl:java.lang.Object
         |[0] o:scala.None
         |[0] tp:scala.AnyRef
         |""".stripMargin)

  // test:runMain com.lightbend.tools.sculpt.Samples "pattern match" "object O { 0 match { case _ => () } }"
  // we only run this on 2.13, because the results are different (and worse) on 2.12, as per
  // https://github.com/lightbend/scala-sculpt/issues/28
  if (!scala.util.Properties.versionNumberString.startsWith("2.12"))
    Sample(
      name = "pattern match",
      source =
        """|object O { 0 match { case _ => () } }""".stripMargin,
      json =
        """|[
           |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["o:O"]},
           |  {"sym": ["o:O", "def:<init>"], "uses": ["o:O"]},
           |  {"sym": ["o:O", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
           |]""".stripMargin,
      classJson =
        """|[
           |  {"sym": ["o:O"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
           |  {"sym": ["o:O"], "uses": ["pkt:scala", "tp:AnyRef"]}
           |]""".stripMargin,
      graph =
        """|Graph 'pattern match': 4 nodes, 3 edges
           |Nodes:
           |  - o:O
           |  - pkt:scala.tp:AnyRef
           |  - o:O.def:<init>
           |  - pkt:java.pkt:lang.cl:Object.def:<init>
           |Edges:
           |  - o:O -[Extends]-> pkt:scala.tp:AnyRef
           |  - o:O.def:<init> -[Uses]-> o:O
           |  - o:O.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>""".stripMargin,
      tree =
        """|pattern match:
           |└── O
           |    └── scala.AnyRef
           |└── scala.AnyRef
           |""".stripMargin,
      cycles =
        """|""".stripMargin,
      layers =
        """|[1] o:O
           |[0] cl:java.lang.Object
           |[0] tp:scala.AnyRef
           |""".stripMargin)

  // this is the sample in the readme
  // test:runMain com.lightbend.tools.sculpt.Samples "readme" "object Dep1 { val x = 42; val y = Dep2.z }; object Dep2 { val z = Dep1.x }"
  Sample(
    name = "readme",
    source =
      """|object Dep1 { val x = 42; val y = Dep2.z }; object Dep2 { val z = Dep1.x }""".stripMargin,
    json =
      """|[
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["o:Dep1"]},
         |  {"sym": ["o:Dep1", "def:<init>"], "uses": ["o:Dep1"]},
         |  {"sym": ["o:Dep1", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["o:Dep1", "def:x"], "uses": ["o:Dep1", "t:x"]},
         |  {"sym": ["o:Dep1", "def:x"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep1", "def:y"], "uses": ["o:Dep1", "t:y"]},
         |  {"sym": ["o:Dep1", "def:y"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep1", "t:x"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep1", "t:y"], "uses": ["o:Dep2", "def:z"]},
         |  {"sym": ["o:Dep1", "t:y"], "uses": ["ov:Dep2"]},
         |  {"sym": ["o:Dep1", "t:y"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"extends": ["pkt:scala", "tp:AnyRef"], "sym": ["o:Dep2"]},
         |  {"sym": ["o:Dep2", "def:<init>"], "uses": ["o:Dep2"]},
         |  {"sym": ["o:Dep2", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["o:Dep2", "def:z"], "uses": ["o:Dep2", "t:z"]},
         |  {"sym": ["o:Dep2", "def:z"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep2", "t:z"], "uses": ["o:Dep1", "def:x"]},
         |  {"sym": ["o:Dep2", "t:z"], "uses": ["ov:Dep1"]},
         |  {"sym": ["o:Dep2", "t:z"], "uses": ["pkt:scala", "cl:Int"]}
         |]""".stripMargin,
    classJson =
      """|[
         |  {"sym": ["o:Dep1"], "uses": ["o:Dep2"]},
         |  {"sym": ["o:Dep1"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["o:Dep1"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep1"], "uses": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["o:Dep2"], "uses": ["o:Dep1"]},
         |  {"sym": ["o:Dep2"], "uses": ["pkt:java", "pkt:lang", "cl:Object"]},
         |  {"sym": ["o:Dep2"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep2"], "uses": ["pkt:scala", "tp:AnyRef"]}
         |]""".stripMargin,
    graph =
      """|Graph 'readme': 15 nodes, 19 edges
         |Nodes:
         |  - o:Dep1
         |  - pkt:scala.tp:AnyRef
         |  - o:Dep1.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |  - o:Dep1.def:x
         |  - o:Dep1.t:x
         |  - pkt:scala.cl:Int
         |  - o:Dep1.def:y
         |  - o:Dep1.t:y
         |  - o:Dep2.def:z
         |  - ov:Dep2
         |  - o:Dep2
         |  - o:Dep2.def:<init>
         |  - o:Dep2.t:z
         |  - ov:Dep1
         |Edges:
         |  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:Dep1.def:<init> -[Uses]-> o:Dep1
         |  - o:Dep1.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - o:Dep1.def:x -[Uses]-> o:Dep1.t:x
         |  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep1.def:y -[Uses]-> o:Dep1.t:y
         |  - o:Dep1.def:y -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep1.t:y -[Uses]-> o:Dep2.def:z
         |  - o:Dep1.t:y -[Uses]-> ov:Dep2
         |  - o:Dep1.t:y -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep2 -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:Dep2.def:<init> -[Uses]-> o:Dep2
         |  - o:Dep2.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - o:Dep2.def:z -[Uses]-> o:Dep2.t:z
         |  - o:Dep2.def:z -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep2.t:z -[Uses]-> o:Dep1.def:x
         |  - o:Dep2.t:z -[Uses]-> ov:Dep1
         |  - o:Dep2.t:z -[Uses]-> pkt:scala.cl:Int""".stripMargin,
    tree =
      """|readme:
         |└── Dep1
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── scala.Int
         |└── Dep2
         |    └── scala.AnyRef
         |""".stripMargin,
    cycles =
      """|[2] o:Dep1 o:Dep2""".stripMargin,
    layers =
      """|[1] o:Dep1 o:Dep2
         |[0] cl:java.lang.Object
         |[0] cl:scala.Int
         |[0] tp:scala.AnyRef
         |""".stripMargin)

}
