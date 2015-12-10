package scala.tools.sculpt
import model._

import org.junit.Test
import org.junit.Assert.assertEquals

class TreeTests {

  def test(sample: Sample): Unit = {
    import spray.json._
    import ModelJsonProtocol._
    val dependencies = sample.json.parseJson.convertTo[Seq[FullDependency]]
    val graph = Graph.apply(sample.name, dependencies)
    assertEquals(sample.tree, TreePrinter(graph))
  }

  @Test def sample1(): Unit =
    test(Sample.sample1)
  @Test def sample2(): Unit =
    test(Sample.sample2)

}
