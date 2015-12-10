package scala.tools.sculpt

object Samples {

  val source1 = "object O"
  val json1 =
    """|[
       |  {"sym": ["cl:O"], "extends": ["pck:scala", "t:AnyRef"]},
       |  {"sym": ["cl:O", "cons"], "uses": ["cl:O"]},
       |  {"sym": ["cl:O", "cons"], "uses": ["pck:java", "pck:lang", "cl:Object", "cons"]}
       |]""".stripMargin

}
