package scala.tools.sculpt

object Samples {

  val source1 = "object O"
  val json1 =
    """|[
       |  {"sym": ["o:O"], "extends": ["pkt:scala", "tp:AnyRef"]},
       |  {"sym": ["o:O", "def:<init>"], "uses": ["o:O"]},
       |  {"sym": ["o:O", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]}
       |]""".stripMargin

}
