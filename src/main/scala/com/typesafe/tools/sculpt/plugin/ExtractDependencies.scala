// Copyright (C) 2015-2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt.plugin

import scala.collection.mutable
import scala.collection.mutable.HashSet
import scala.io.Codec
import scala.tools.nsc
import nsc.plugins._
import scala.reflect.internal.Flags.{PACKAGE}
import com.typesafe.tools.sculpt.model._
import spray.json._
import com.typesafe.tools.sculpt.model.ModelJsonProtocol._
import java.io.File

// adapted from the incremental compiler
abstract class ExtractDependencies extends PluginComponent {
  import global._

  /** The output file to write to, or None for stdout */
  def outputPath: Option[File]

  def classMode: Boolean

  override def description = "Extract Dependency Phase for Scala Sculpt"

  /** the following two members override abstract members in Transform */
  val phaseName: String = "extract-deps"

  /** Create a new phase which applies transformer */
  def newPhase(prev: nsc.Phase): StdPhase = new Phase(prev)

  type MultiMapIterator = Iterator[(Symbol, HashSet[Symbol])]

  /** The phase defined by this transform */
  class Phase(prev: nsc.Phase) extends StdPhase(prev) {
    private var extractDependenciesTraverser: ExtractDependenciesTraverser = null

    override def run(): Unit = {
      extractDependenciesTraverser = new ExtractDependenciesTraverser
      super.run
      val deps = extractDependenciesTraverser.dependencies
      val inheritDeps = extractDependenciesTraverser.inheritanceDependencies
      extractDependenciesTraverser = null
      val fullDependencies =
        (createFullDependencies(deps, DependencyKind.Uses) ++ createFullDependencies(inheritDeps, DependencyKind.Extends))
          .filterNot(d => d.from == d.to)
          .groupBy(identity).map { case (d, l) => d.copy(count = l.size) }.toSeq.sortBy(_.toString)
      val json =
        if (classMode)
          ClassMode(fullDependencies).toJson
        else
          fullDependencies.toJson
      writeOutput(FullDependenciesPrinter(json))
    }

    def apply(unit: CompilationUnit) = extractDependenciesTraverser.traverse(unit.body)

    def writeOutput(s: String): Unit = {
      outputPath match {
        case Some(f) => new scala.reflect.io.File(f)(Codec.UTF8).writeAll(s)
        case None => print(s)
      }
    }

    def createFullDependencies(syms: MultiMapIterator, kind: DependencyKind): Seq[FullDependency] = {
      def entitiesFor(s: Symbol) =
        Path(s.ownerChain.reverse.dropWhile(s => s.isEffectiveRoot || s.isEmptyPackage).map(Entity.forSymbol _))
      (for {
        (from, tos) <- syms
        fromEntities = entitiesFor(from)
        to <- tos
      } yield FullDependency(fromEntities, entitiesFor(to), kind, 1)).toSeq
    }
  }

  private class ExtractDependenciesTraverser extends Traverser {
    import collection.mutable.{HashMap, HashSet}
    private def emptyMultiMap: mutable.Map[Symbol, HashSet[Symbol]] = HashMap.empty[Symbol, HashSet[Symbol]].withDefault( _ => HashSet.empty[Symbol])

    private val deps = emptyMultiMap
    protected def addDependency(dep: Symbol): Unit = if (dep ne NoSymbol) deps(currentOwner) +== dep
    def dependencies: MultiMapIterator = deps.iterator

    private val inheritanceDeps = emptyMultiMap
    protected def addInheritanceDependency(dep: Symbol): Unit = if (dep ne NoSymbol) inheritanceDeps(currentOwner) +== dep
    def inheritanceDependencies: MultiMapIterator = inheritanceDeps.iterator

    /*
     * Some macros appear to contain themselves as original tree.
     * We must check that we don't inspect the same tree over and over.
     * See https://issues.scala-lang.org/browse/SI-8486
     *     https://github.com/sbt/sbt/issues/1237
     *     https://github.com/sbt/sbt/issues/1544
     */
    private val inspectedOriginalTrees = collection.mutable.Set.empty[Tree]

    object MacroExpansionOf {
      def unapply(tree: Tree): Option[Tree] =
        tree.attachments.all.collect {
          case att: analyzer.MacroExpansionAttachment => att.expandee
        }.headOption
    }

    // skip packages
    private def symbolsInType(tp: Type) = tp.collect{ case tp if tp != null && !(tp.typeSymbolDirect hasFlag PACKAGE) => tp.typeSymbolDirect }.toSet
    private def flattenTypeToSymbols(tp: Type): List[Symbol] = if (tp eq null) Nil else tp match {
      case ct: CompoundType => ct.typeSymbolDirect :: ct.parents.flatMap(flattenTypeToSymbols)
      case _ => List(tp.typeSymbolDirect)
    }

    override def traverse(tree: Tree): Unit =
      tree match {
        /*
         * Idents are used in number of situations:
         *  - to refer to local variable
         *  - to refer to a top-level package (other packages are nested selections)
         *  - to refer to a term defined in the same package as an enclosing class;
         *    this looks fishy, see this thread:
         *    https://groups.google.com/d/topic/scala-internals/Ms9WUAtokLo/discussion
         *
         *  Select
         *
         *  SelectFromTypeTree
         */
        case id: Ident => addDependency(id.symbol)
        case sel@Select(qual, _) => traverse(qual) ; addDependency(sel.symbol)
        case sel@SelectFromTypeTree(qual, _) => traverse(qual) ; addDependency(sel.symbol)

        // In some cases (eg. macro annotations), `typeTree.tpe` may be null.
        // See sbt/sbt#1593 and sbt/sbt#1655.
        case typeTree: TypeTree if typeTree.tpe != null =>
          symbolsInType(typeTree.tpe).foreach(addDependency)

        case Template(parents, self, body) =>
          val inheritanceTypes = (self.tpt.tpe :: parents.map(_.tpe)).toSet
          val inheritanceSymbols = inheritanceTypes.flatMap(flattenTypeToSymbols)
          inheritanceSymbols.foreach(addInheritanceDependency)

          val allSymbols = inheritanceTypes.flatMap(symbolsInType)
          (allSymbols -- inheritanceSymbols).foreach(addDependency)
          traverseTrees(body)

        case MacroExpansionOf(original) if inspectedOriginalTrees.add(original) => traverse(original)

          // imports are a separate issue (remove unused ones, rewrite ones that were moved)
//        case Import(expr, selectors) =>
//          selectors.foreach {
//            case ImportSelector(nme.WILDCARD, _, null, _) =>
//            // in case of wildcard import we do not rely on any particular name being defined
//            // on `expr`; all symbols that are being used will get caught through selections
//            case ImportSelector(name: Name, _, _, _) =>
//              def lookupImported(name: Name) = expr.symbol.info.member(name)
//              // importing a name means importing both a term and a type (if they exist)
//              addDependency(lookupImported(name.toTermName))
//              addDependency(lookupImported(name.toTypeName))
//          }

        case _ => super.traverse(tree)
      }

  }

}
