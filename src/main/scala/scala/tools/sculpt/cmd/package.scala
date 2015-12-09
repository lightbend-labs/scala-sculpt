package scala.tools.sculpt

import spray.json._
import scala.io.Codec
import scala.tools.sculpt.model._
import scala.tools.sculpt.model.ModelJsonProtocol._

/** REPL commands and features */
package object cmd {
  /** Load a Sculpt model JSON file and return the model */
  def load(path: String): Seq[FullDependency] = {
    val s = new scala.reflect.io.File(new java.io.File(path))(Codec.UTF8).slurp()
    s.parseJson.convertTo[Seq[FullDependency]]
  }
}
