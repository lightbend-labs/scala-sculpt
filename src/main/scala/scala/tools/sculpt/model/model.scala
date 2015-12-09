package scala.tools.sculpt.model

import scala.reflect.internal.Symbols

sealed trait EntityKind
object EntityKind {
  case object Trait extends EntityKind
  case object Class extends EntityKind
  case object Type extends EntityKind
  case object Var extends EntityKind
  case object Package extends EntityKind
  case object Object extends EntityKind
  case object Constructor extends EntityKind
  case object Def extends EntityKind
  case object Val extends EntityKind
  case object Unknown extends EntityKind
}

sealed trait DependencyKind
object DependencyKind {
  case object Extends extends DependencyKind
  case object Uses extends DependencyKind
}

case class Entity(name: String, kind: EntityKind) {
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

case class FullDependency(from: Seq[Entity], to: Seq[Entity], kind: DependencyKind, count: Int) {
  override def toString =
    from.mkString(".") + " " + kind.toString.toLowerCase + " " + to.mkString(".") + s" [$count]"
}
