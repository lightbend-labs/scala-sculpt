// Copyright (C) 2015-2022 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.model

import scala.reflect.internal.Symbols

sealed trait EntityKind {
  def prefix: String
}
object EntityKind {
  sealed abstract class AnyTerm(val prefix: String) extends EntityKind
  sealed abstract class AnyType(val prefix: String) extends EntityKind

  case object Module extends AnyTerm("ov")
  case object Method extends AnyTerm("def")
  case object Mutable extends AnyTerm("var")
  case object Macro extends AnyTerm("mac")
  case object Package extends AnyTerm("pk")
  case object Term extends AnyTerm("t")

  case object Trait extends AnyType("tr")
  case object PackageType extends AnyType("pkt")
  case object ModuleClass extends AnyType("o")
  case object Class extends AnyType("cl")
  case object Type extends AnyType("tp")
}

sealed trait DependencyKind
object DependencyKind {
  case object Extends extends DependencyKind
  case object Uses extends DependencyKind
}

case class Entity(name: String, kind: EntityKind) {
  override def toString = kind.prefix + ":" + name
}

case class Path(elems: Seq[Entity]) {
  override def toString = elems.mkString(".")
  def kindString = elems.lastOption.map(_.kind.prefix).getOrElse("")
  def nameString = elems.map(_.name).mkString(".")
  def simpleString = s"$kindString:$nameString"
}

object Entity {
  def forSymbol(sym: Symbols#Symbol): Entity = {
    val kind =
      if (sym.isType) {
        if (sym.isTrait) EntityKind.Trait
        else if (sym.hasPackageFlag) EntityKind.PackageType
        else if (sym.isModuleClass) EntityKind.ModuleClass
        else if (sym.isClass) EntityKind.Class
        else EntityKind.Type
      }
      else { // Term
        if (sym.hasPackageFlag) EntityKind.Package
        else if (sym.isTermMacro) EntityKind.Macro
        else if (sym.isModule) EntityKind.Module
        else if (sym.isMethod) EntityKind.Method
        else if (sym.isVariable) EntityKind.Mutable
        else EntityKind.Term
      }
    Entity(sym.nameString, kind)
  }
}

case class FullDependency(from: Path, to: Path, kind: DependencyKind, count: Int) {
  override def toString = {
    val s = s"$from ${kind.toString.toLowerCase} $to"
    if (count == 1) s else s"$s [$count]"
  }
}
