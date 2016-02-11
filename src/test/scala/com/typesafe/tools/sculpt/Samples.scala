// Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt

case class Sample(
  name: String,
  source: String,
  json: String,
  graph: Option[String],  // Option because it can get tedious to always include
  tree: String
) {
  Samples.samples += this
}

object Samples {

  val samples = collection.mutable.Buffer.empty[Sample]

  Sample(
    name = "lone object",
    source = "object O",
    json =
      """|[
         |  {"sym": ["o:O"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["o:O"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
         |]""".stripMargin,
    graph = Some(
      """|Graph 'lone object': 4 nodes, 3 edges
         |Nodes:
         |  - o:O
         |  - pkt:scala.tp:AnyRef
         |  - o:O.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |Edges:
         |  - o:O -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:O.def:<init> -[Uses]-> o:O
         |  - o:O.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>""".stripMargin),
    tree = """|lone object:
              |└── O
              |    └── scala.AnyRef
              |└── scala.AnyRef
              |""".stripMargin)

  Sample(
    name = "two subclasses",
    source = "trait T; class C1 extends T; class C2 extends T",
    json =
      """|[
         |  {"sym": ["cl:C1"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["cl:C1"], "extends": ["tr:T"]},
         |  {"sym": ["cl:C1", "def:<init>"], "uses": ["cl:C1"]},
         |  {"sym": ["cl:C1", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["cl:C2"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["cl:C2"], "extends": ["tr:T"]},
         |  {"sym": ["cl:C2", "def:<init>"], "uses": ["cl:C2"]},
         |  {"sym": ["cl:C2", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["tr:T"], "extends": ["pkt:scala", "tp:AnyRef"]}
         |]""".stripMargin,
    graph = Some(
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
         |  - tr:T -[Extends]-> pkt:scala.tp:AnyRef""".stripMargin),
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
         |""".stripMargin)

  Sample(
    name = "circular dependency",
    source = "trait T1 { def x: T2 }; trait T2 { def x: T1 }",
    json =
      """|[
         |  {"sym": ["tr:T1"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["tr:T1", "def:x"], "uses": ["tr:T2"]},
         |  {"sym": ["tr:T2"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["tr:T2", "def:x"], "uses": ["tr:T1"]}
         |]""".stripMargin,
    graph = Some(
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
         |  - tr:T2.def:x -[Uses]-> tr:T1""".stripMargin),
    tree =
      """|circular dependency:
         |└── T1
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── T2
         |    └── scala.AnyRef
         |""".stripMargin)

  Sample(
    name = "package",
    source = "package a.b { class C1; class C2 }",
    json =
      """|[
         |  {"sym": [], "uses": ["pk:a"]},
         |  {"sym": [], "uses": ["pkt:a", "pk:b"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C1"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C1", "def:<init>"], "uses": ["pkt:a", "pkt:b", "cl:C1"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C1", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C2"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C2", "def:<init>"], "uses": ["pkt:a", "pkt:b", "cl:C2"]},
         |  {"sym": ["pkt:a", "pkt:b", "cl:C2", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
         |]""".stripMargin,
    graph = Some(
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
         |  - pkt:a.pkt:b.cl:C2.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>""".stripMargin),
    tree =
      """|package:
         |└── a.b.C1
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── a.b.C2
         |    └── scala.AnyRef
         |""".stripMargin)

  // this is the sample in the readme
  Sample(
    name = "readme",
    source = "object Dep1 { final val x = 42 }; object Dep2 { val x = Dep1.x }",
    json =
      """|[
         |  {"sym": ["o:Dep1"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["o:Dep1", "def:<init>"], "uses": ["o:Dep1"]},
         |  {"sym": ["o:Dep1", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["o:Dep1", "def:x"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep1", "t:x"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep2"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["o:Dep2", "def:<init>"], "uses": ["o:Dep2"]},
         |  {"sym": ["o:Dep2", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
         |  {"sym": ["o:Dep2", "def:x"], "uses": ["o:Dep2", "t:x"]},
         |  {"sym": ["o:Dep2", "def:x"], "uses": ["pkt:scala", "cl:Int"]},
         |  {"sym": ["o:Dep2", "t:x"], "uses": ["pkt:scala", "cl:Int"]}
         |]""".stripMargin,
    graph = Some(
      """|Graph 'readme': 11 nodes, 11 edges
         |Nodes:
         |  - o:Dep1
         |  - pkt:scala.tp:AnyRef
         |  - o:Dep1.def:<init>
         |  - pkt:java.pkt:lang.cl:Object.def:<init>
         |  - o:Dep1.def:x
         |  - pkt:scala.cl:Int
         |  - o:Dep1.t:x
         |  - o:Dep2
         |  - o:Dep2.def:<init>
         |  - o:Dep2.def:x
         |  - o:Dep2.t:x
         |Edges:
         |  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:Dep1.def:<init> -[Uses]-> o:Dep1
         |  - o:Dep1.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep2 -[Extends]-> pkt:scala.tp:AnyRef
         |  - o:Dep2.def:<init> -[Uses]-> o:Dep2
         |  - o:Dep2.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
         |  - o:Dep2.def:x -[Uses]-> o:Dep2.t:x
         |  - o:Dep2.def:x -[Uses]-> pkt:scala.cl:Int
         |  - o:Dep2.t:x -[Uses]-> pkt:scala.cl:Int""".stripMargin),
    tree =
      """|readme:
         |└── Dep1
         |    └── scala.AnyRef
         |└── scala.AnyRef
         |└── scala.Int
         |└── Dep2
         |    └── scala.AnyRef
         |""".stripMargin)

}
