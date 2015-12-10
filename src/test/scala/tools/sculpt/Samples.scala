package scala.tools.sculpt

case class Sample(
  name: String,
  source: String,
  json: String,
  graph: Option[String],  // too tedious to have every time
  tree: String)

object Sample {

  val sample1 = Sample(
    name = "lone object",
    source = "object O",
    json =
      """|[
         |  {"sym": ["o:O"], "extends": ["pkt:scala", "tp:AnyRef"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["o:O"]},
         |  {"sym": ["o:O", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
         |]""".stripMargin,
    graph = Some(
      """|Graph 'lone object': 4 nodes, 3 edges""".stripMargin),
    tree = """|lone object:
              |└── O
              |    └── scala.AnyRef
              |└── scala.AnyRef
              |""".stripMargin)

  val sample2 = Sample(
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
    graph = None,
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

}
