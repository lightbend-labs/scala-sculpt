package scala.tools.sculpt

import model._

import org.junit.Test
import org.junit.Assert.assertEquals

import spray.json._

class SerializationTests {

  // Stefan didn't like the appearance of the output with either of
  // spray-json's formatters (`compactPrint` and `prettyPrint`),
  // so he rolled his own in FullDependenciesPrinter
  def print(js: JsValue): String = {
    val sb = new java.lang.StringBuilder
    FullDependenciesPrinter.print(js, sb)
    sb.toString
  }

  // JSON -> JSValue -> JSON
  @Test def roundTripThroughJsonASTs(): Unit = {
    assertEquals(Samples.json1,
      print(Samples.json1.parseJson))
  }

  // JSON -> JSValue -> Seq[FullDependency] -> JSValue -> JSON
  @Test def roundTripThroughModel(): Unit = {
    import ModelJsonProtocol._
    val dependencies =
      Samples.json1.parseJson.convertTo[Seq[FullDependency]]
    assertEquals(Samples.json1, print(dependencies.toJson))
  }

}
