// Copyright (C) 2015-2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt.model

import spray.json._
import com.typesafe.tools.sculpt.util.RegexInterpolator

/** JSON serialization/deserialization for the Sculpt model types */
object ModelJsonProtocol extends DefaultJsonProtocol {

  implicit object entityFormat extends JsonFormat[Entity] {
    def write(e: Entity) = new JsString(e.kind.prefix + ":" + e.name)
    def read(value: JsValue) = value.convertTo[String] match {
      case r"ov:(.*)$n"  => Entity(n, EntityKind.Module)
      case r"def:(.*)$n" => Entity(n, EntityKind.Method)
      case r"var:(.*)$n" => Entity(n, EntityKind.Mutable)
      case r"mac:(.*)$n" => Entity(n, EntityKind.Macro)
      case r"pk:(.*)$n" =>  Entity(n, EntityKind.Package)
      case r"t:(.*)$n"   => Entity(n, EntityKind.Term)
      case r"tr:(.*)$n"  => Entity(n, EntityKind.Trait)
      case r"pkt:(.*)$n" => Entity(n, EntityKind.PackageType)
      case r"o:(.*)$n"   => Entity(n, EntityKind.ModuleClass)
      case r"cl:(.*)$n"  => Entity(n, EntityKind.Class)
      case r"tp:(.*)$n"  => Entity(n, EntityKind.Type)
      case _ => throw new DeserializationException("'EntityKind:Name' string expected")
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
