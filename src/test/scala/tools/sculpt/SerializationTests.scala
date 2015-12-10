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
  def roundTripThroughJsonASTs(sample: Sample): Unit = {
    assertEquals(sample.json,
      print(sample.json.parseJson))
  }

  // JSON -> JSValue -> Seq[FullDependency] -> JSValue -> JSON
  def roundTripThroughModel(sample: Sample): Unit = {
    import ModelJsonProtocol._
    val dependencies =
      sample.json.parseJson.convertTo[Seq[FullDependency]]
    assertEquals(sample.json, print(dependencies.toJson))
  }

  @Test def sample1a(): Unit =
    roundTripThroughJsonASTs(Sample.sample1)
  @Test def sample1b(): Unit =
    roundTripThroughModel(Sample.sample1)

  @Test def sample2a(): Unit =
    roundTripThroughJsonASTs(Sample.sample2)
  @Test def sample2b(): Unit =
    roundTripThroughModel(Sample.sample2)

}
