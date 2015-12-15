package scala.tools.sculpt
import model._

import org.junit.Test
import org.junit.Assert.assertEquals

class GraphTests {

  def test(sample: Sample): Unit = {
    import spray.json._
    import ModelJsonProtocol._
    val dependencies = sample.json.parseJson.convertTo[Seq[FullDependency]]
    val graph = Graph.apply(sample.name, dependencies)
    assertEquals(sample.graph.get, graph.fullString)
  }

  @Test def sample1(): Unit =
    test(Sample.sample1)
  @Test def sample5(): Unit =
    test(Sample.sample5)

}
