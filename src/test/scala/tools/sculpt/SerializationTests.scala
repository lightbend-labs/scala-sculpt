package scala.tools.sculpt

import model._
import org.scalatest.FunSuite
import spray.json._

class SerializationTests extends FunSuite {

  // Stefan didn't like the appearance of the output with either of
  // spray-json's formatters (`compactPrint` and `prettyPrint`),
  // so he rolled his own in FullDependenciesPrinter
  def print(js: JsValue): String = {
    val sb = new java.lang.StringBuilder
    FullDependenciesPrinter.print(js, sb)
    sb.toString
  }

  // JSON -> JSValue -> JSON
  def roundTripThroughJsonASTs(sample: Sample): Unit = {
    assertResult(sample.json) {
      print(sample.json.parseJson)
    }
  }

  // JSON -> JSValue -> Seq[FullDependency] -> JSValue -> JSON
  def roundTripThroughModel(sample: Sample): Unit = {
    import ModelJsonProtocol._
    val dependencies =
      sample.json.parseJson.convertTo[Seq[FullDependency]]
    assertResult(sample.json) {
      print(dependencies.toJson)
    }
  }

  for (sample <- Sample.samples) {
    test(s"${sample.name}: through ASTs") {
      roundTripThroughJsonASTs(sample)
    }
    test(s"${sample.name}: through model") {
      roundTripThroughModel(sample)
    }
  }

}
