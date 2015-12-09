package scala.tools.sculpt

package object util {
  implicit class RegexInterpolator(sc: StringContext) {
    def r = new scala.util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }
}
