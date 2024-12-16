// Copyright (C) Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.model

import spray.json._

/** JSON serialization/deserialization for the Sculpt model types */
object ModelJsonProtocol extends DefaultJsonProtocol {

  implicit object entityFormat extends JsonFormat[Entity] {
    def write(e: Entity) = new JsString(e.kind.prefix + ":" + e.name)

    private object Kind {
      def unapply(str: String): Option[EntityKind] = str match {
        case "ov"  => Some(EntityKind.Module)
        case "def" => Some(EntityKind.Method)
        case "var" => Some(EntityKind.Mutable)
        case "mac" => Some(EntityKind.Macro)
        case "pk"  => Some(EntityKind.Package)
        case "t"   => Some(EntityKind.Term)
        case "tr"  => Some(EntityKind.Trait)
        case "pkt" => Some(EntityKind.PackageType)
        case "o"   => Some(EntityKind.ModuleClass)
        case "cl"  => Some(EntityKind.Class)
        case "tp"  => Some(EntityKind.Type)
        case _     => None
      }
    }
    def read(value: JsValue) = {
      val valueString = value.convertTo[String]
      val idx = valueString.indexOf(':')
      valueString.splitAt(idx) match {
        case (Kind(kind), n) =>
          Entity(n.tail, kind) // n includes ':' so take tail
        case _ => throw new DeserializationException("'EntityKind:Name' string expected")
      }
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
          case DependencyKind.Uses    => "uses"
        }) -> d.to.toJson
      )
      JsObject((if (d.count == 1) data else data :+ ("count" -> JsNumber(d.count))): _*)
    }
    def read(value: JsValue) = {
      val m = value.asJsObject.fields
      val from = m("sym").convertTo[Path]
      val (kind, to) =
        if (m.contains("uses")) (DependencyKind.Uses, m("uses").convertTo[Path])
        else (DependencyKind.Extends, m("extends").convertTo[Path])
      val count = m.get("count").map(_.convertTo[Int]).getOrElse(1)
      FullDependency(from, to, kind, count)
    }
  }
}
