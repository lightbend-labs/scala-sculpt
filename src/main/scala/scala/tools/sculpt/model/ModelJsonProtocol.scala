package scala.tools.sculpt.model

import spray.json._
import scala.tools.sculpt.util.RegexInterpolator

/** JSON serialization/deserialization for the Sculpt model types */
object ModelJsonProtocol extends DefaultJsonProtocol {

  implicit object entityFormat extends JsonFormat[Entity] {
    def write(e: Entity) = new JsString(e.kind match {
      case EntityKind.Constructor => e.kind.prefix
      case k => k.prefix + ":" + e.name
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

  implicit object pathFormat extends JsonFormat[Path] {
    def write(p: Path) = p.elems.toJson
    def read(value: JsValue) = Path(value.convertTo[Vector[Entity]])
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
      val from = m("sym").convertTo[Path]
      val (kind, to) =
        if(m.contains("uses")) (DependencyKind.Uses, m("uses").convertTo[Path])
        else (DependencyKind.Extends, m("extends").convertTo[Path])
      val count = m.get("count").map(_.convertTo[Int]).getOrElse(1)
      FullDependency(from, to, kind, count)
    }
  }
}
