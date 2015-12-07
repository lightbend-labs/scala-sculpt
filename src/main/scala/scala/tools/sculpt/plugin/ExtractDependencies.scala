package scala.tools.sculpt.plugin

import scala.collection.mutable
import scala.tools.nsc
import nsc.plugins._
import scala.reflect.internal.Flags.{PACKAGE}

// adapted from the incremental compiler
abstract class ExtractDependencies extends PluginComponent {
  import global._

  override def description = "Extract Dependency Phase for Scala Sculpt"

  /** the following two members override abstract members in Transform */
  val phaseName: String = "extract-deps"

  /** Create a new phase which applies transformer */
  def newPhase(prev: nsc.Phase): StdPhase = new Phase(prev)

  /** The phase defined by this transform */
  class Phase(prev: nsc.Phase) extends StdPhase(prev) {
    def apply(unit: CompilationUnit) = {
      val extractDependenciesTraverser = new ExtractDependenciesTraverser
      extractDependenciesTraverser.traverse(unit.body)
      val deps = extractDependenciesTraverser.dependencies
      deps foreach { case (from, tos) => println(s"REG $from --> $tos")}
      val inheritDeps = extractDependenciesTraverser.inheritanceDependencies
      inheritDeps foreach { case (from, tos) => println(s"INH $from --> $tos")}
    }
  }

  private class ExtractDependenciesTraverser extends Traverser {
    import collection.mutable.{HashMap, HashSet}
    private def emptyMultiMap: mutable.Map[Symbol, HashSet[Symbol]] = HashMap.empty[Symbol, HashSet[Symbol]].withDefault( _ => HashSet.empty[Symbol])

    type MultiMapIterator = Iterator[(Symbol, HashSet[Symbol])]

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
    private def symbolsInType(tp: Type) = tp.collect{ case tp if !(tp.typeSymbol hasFlag PACKAGE) => tp.typeSymbolDirect }.toSet
    private def flattenTypeToSymbols(tp: Type): List[Symbol] = tp match {
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
