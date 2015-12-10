package scala.tools.sculpt

object Samples {

  val source1 = "object O"
  val json1 =
    """|[
       |  {"sym": ["o:O"], "extends": ["pck:scala", "t:AnyRef"]},
       |  {"sym": ["o:O", "cons"], "uses": ["o:O"]},
       |  {"sym": ["o:O", "cons"], "uses": ["pck:java", "pck:lang", "cl:Object", "cons"]}
       |]""".stripMargin

}
