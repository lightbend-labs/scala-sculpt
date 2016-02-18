// Copyright (C) 2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt.model

object ClassMode {

  // promotes all of the dependencies to class level, discarding self-dependencies,
  // collapsing the uses/extends distinction, setting all counts to 1, and eliminating
  // duplicates. (real counts handling could be a future todo.)  sorting the output
  // helps testability and helps human-readability of the resulting JSON.

  def apply(deps: Seq[FullDependency]): Seq[FullDependency] = {
    val promoted =
      for {
        dep <- deps
        from <- promote(dep.from)
        to <- promote(dep.to)
        if from != to
      } yield FullDependency(from = from, to = to, kind = DependencyKind.Uses, count = 1)
    promoted.distinct.sortBy(_.toString)
  }

  // Throws away too-specific stuff at the end of a path, so we end up with
  // a class-level path (or None).  The too-specific stuff might be a method,
  // inner class, etc.

  def promote(path: Path): Option[Path] =
    path.elems.span(_.kind == EntityKind.PackageType) match {
      case (packages, clazz +: _) =>
        assert(isClassKind(clazz.kind),
          s"unexpected entity kind after packages in $path")
        Some(Path(packages :+ clazz))
      case _ =>
        None
    }

  // The inclusion of EntityKind.Type may seem questionable, but it's needed
  // in order to pull in things like `extends scala.AnyRef` since AnyRef is
  // just a type alias for `java.lang.Object`

  private val isClassKind: EntityKind => Boolean =
    Set[EntityKind](
      EntityKind.Trait, EntityKind.Class,
      EntityKind.Module, EntityKind.ModuleClass,
      EntityKind.Type)

}
