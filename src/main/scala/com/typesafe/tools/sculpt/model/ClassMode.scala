// Copyright (C) 2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt.model

object ClassMode {
  def apply(deps: Seq[FullDependency]): Seq[FullDependency] = {
    def isClassKind(kind: EntityKind): Boolean =
      Seq(EntityKind.Trait, EntityKind.Class, EntityKind.ModuleClass, EntityKind.Type)
        .contains(kind)
    def promote(path: Path): Option[Path] = {
      val (packages, rest) = path.elems.span(_.kind == EntityKind.PackageType)
      if (rest.nonEmpty && isClassKind(rest.head.kind))
        Some(Path(packages :+ rest.head))
      else
        None
    }
    val candidates =
      for {
        dep <- deps
        from <- promote(dep.from)
        to <- promote(dep.to)
        if from != to
      } yield FullDependency(from = from, to = to, kind = DependencyKind.Uses, count = 1)
    candidates.distinct
      .sortBy(_.toString)  // helps human-readability
  }
}
