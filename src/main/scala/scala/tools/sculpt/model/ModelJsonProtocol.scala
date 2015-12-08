package scala.tools.sculpt.model

import spray.json._

object ModelJsonProtocol extends DefaultJsonProtocol {
  private def enumFormat[T <: Enumeration](enum: T): JsonFormat[T#Value] = new JsonFormat[T#Value] {
    def write(v: T#Value) = JsString(v.toString)
    def read(v: JsValue) = enum.withName(v.asInstanceOf[JsString].value)
  }

  implicit val entityKindFormat = enumFormat(EntityKind)
  implicit val dependencyKindFormat = enumFormat(DependencyKind)
  implicit val entityFormat = jsonFormat2(Entity.apply)
  implicit val fullDependencyFormat = jsonFormat4(FullDependency.apply)
}
