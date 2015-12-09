package scala.tools.sculpt.model

import spray.json._
import scala.tools.sculpt.util.RegexInterpolator

/** JSON serialization/deserialization for the Sculpt model types */
object ModelJsonProtocol extends DefaultJsonProtocol {

  implicit object entityFormat extends JsonFormat[Entity] {
    def write(e: Entity) = new JsString(e.kind match {
      case EntityKind.Trait       => "tr:" + e.name
      case EntityKind.Class       => "cl:" + e.name
      case EntityKind.Type        => "t:" + e.name
      case EntityKind.Var         => "var:" + e.name
      case EntityKind.Package     => "pck:" + e.name
      case EntityKind.Object      => "o:" + e.name
      case EntityKind.Constructor => "cons"
      case EntityKind.Def         => "def:" + e.name
      case EntityKind.Val         => "val:" + e.name
      case EntityKind.Unknown     => "u:" + e.name
    })
    def read(value: JsValue) = value.convertTo[String] match {
      case r"tr:(.*)$n"  => Entity(n, EntityKind.Trait)
      case r"cl:(.*)$n"  => Entity(n, EntityKind.Class)
      case r"t:(.*)$n"   => Entity(n, EntityKind.Type)
      case r"var:(.*)$n" => Entity(n, EntityKind.Var)
      case r"pck:(.*)$n" => Entity(n, EntityKind.Package)
      case r"o:(.*)$n"   => Entity(n, EntityKind.Object)
      case "cons"        => Entity("<init>", EntityKind.Constructor)
      case r"def:(.*)$n" => Entity(n, EntityKind.Def)
      case r"val:(.*)$n" => Entity(n, EntityKind.Val)
      case r"u:(.*)$n"   => Entity(n, EntityKind.Unknown)
      case _ => throw new DeserializationException("Entity string expected")
    }
  }

  implicit object fullDependencyFormat extends JsonFormat[FullDependency] {
    def write(d: FullDependency) = {
      val data = Seq(
        "sym" -> d.from.toJson,
        (d.kind match {
          case DependencyKind.Extends => "extends"
          case DependencyKind.Uses => "uses"
        }) -> d.to.toJson
      )
      JsObject((if(d.count == 1) data else data :+ ("count" -> JsNumber(d.count))): _*)
    }
    def read(value: JsValue) = {
      val m = value.asJsObject.fields
      val from = m("sym").convertTo[Vector[Entity]]
      val (kind, to) =
        if(m.contains("uses")) (DependencyKind.Uses, m("uses").convertTo[Vector[Entity]])
        else (DependencyKind.Extends, m("extends").convertTo[Vector[Entity]])
      val count = m.get("count").map(_.convertTo[Int]).getOrElse(1)
      FullDependency(from, to, kind, count)
    }
  }
}
