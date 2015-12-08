package scala.tools.sculpt.model

import scala.reflect.internal.Symbols

object EntityKind extends Enumeration {
  val Trait, Class, Type, Var, Package, Object, Constructor, Def, Val, Unknown = Value
}

object DependencyKind extends Enumeration {
  val Extends, Uses = Value
}

case class Entity(name: String, kind: EntityKind.Value) {
  override def toString = "(" + kind.toString.toLowerCase + ")" + name
}

object Entity {
  def forSymbol(sym: Symbols#Symbol): Entity = {
    val kind = if(sym.isJavaInterface || sym.isTrait && !sym.isImplClass) EntityKind.Trait
      else if(sym.hasPackageFlag) EntityKind.Package
      else if(sym.isClass) EntityKind.Class
      else if(sym.isType && !sym.isParameter) EntityKind.Type
      else if(sym.isVariable) EntityKind.Var
      else if(sym.isModule) EntityKind.Object
      else if(sym.isClassConstructor) EntityKind.Constructor
      else if(sym.isSourceMethod) EntityKind.Def
      else if(sym.isTerm && (!sym.isParameter || sym.isParamAccessor)) EntityKind.Val
      else EntityKind.Unknown
    Entity(sym.nameString, kind)
  }
}

case class FullDependency(from: Seq[Entity], to: Seq[Entity], kind: DependencyKind.Value, count: Option[Int]) {
  override def toString =
    from.mkString(".") + " " + kind.toString.toLowerCase + " " + to.mkString(".") + count.map(c => s" [x$c]").getOrElse("")
}
