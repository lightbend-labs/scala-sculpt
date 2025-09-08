// Copyright (C) Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.model

object ClassMode {

  // promotes all of the dependencies to class level:
  // * discarding self-dependencies
  // * collapsing the uses/extends distinction
  // * collapsing the Module/ModuleClass distinction
  // * ignoring irrelevant pseudo-dependencies
  // * setting all counts to 1
  //   (real counts handling is possible future work)
  // * eliminating duplicates
  // * sorting, for testability and human-readability

  def apply(deps: Seq[FullDependency]): Seq[FullDependency] = {
    val promoted =
      for {
        dep <- deps
        from <- promote(dep.from)
        to <- promote(dep.to)
        if from != to
      } yield FullDependency(
        from = from,
        to = to,
        kind = DependencyKind.Uses,
        count = 1)
    promoted.distinct.sortBy(_.toString)
  }

  // Throws away too-specific stuff at the end of a path, so we end up with
  // a class-level path (or None).  The too-specific stuff might be a method,
  // inner class, etc.

  def promote(path: Path): Option[Path] =
    path.elems.span(isPackage) match {
      case (packages, next +: _) =>
        next.kind match {
          case EntityKind.Trait =>
            Some(Path(packages :+ next.copy(kind = EntityKind.Class)))
          // collapse Module/ModuleClass distinction
          case EntityKind.Module | EntityKind.ModuleClass =>
            Some(Path(packages :+ next.copy(kind = EntityKind.Class)))
          case k if isClassKind(k) =>
            Some(Path(packages :+ next))
          // ignore strange dependencies on bare terms;
          // see https://github.com/lightbend/scala-sculpt/issues/28
          case EntityKind.Term if packages.isEmpty =>
            None
          case _ =>
            throw new IllegalArgumentException(
              s"unexpected entity kind after packages in $path")
        }
      case _ =>
        None
    }

  def isPackage(entity: Entity): Boolean =
    entity.kind == EntityKind.Package || entity.kind == EntityKind.PackageType

  // The inclusion of EntityKind.Type may seem questionable, but it's needed
  // in order to pull in things like `extends scala.AnyRef` since AnyRef is
  // just a type alias for `java.lang.Object`

  private val isClassKind: EntityKind => Boolean =
    Set[EntityKind](
      EntityKind.Trait,
      EntityKind.Class,
      EntityKind.ModuleClass,
      EntityKind.Type)

}
