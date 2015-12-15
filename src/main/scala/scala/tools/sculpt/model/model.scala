package scala.tools.sculpt.model

import scala.StringBuilder
import scala.collection.mutable
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
  def simpleString = elems.map(_.name).mkString(".")
}

object Entity {
  def forSymbol(sym: Symbols#Symbol): Entity = {
    val kind = if(sym.isType) {
      if(sym.isTrait) EntityKind.Trait
      else if(sym.hasPackageFlag) EntityKind.PackageType
      else if(sym.isModuleClass) EntityKind.ModuleClass
      else if(sym.isClass) EntityKind.Class
      else EntityKind.Type
    } else { // Term
      if(sym.hasPackageFlag) EntityKind.Package
      else if(sym.isTermMacro) EntityKind.Macro
      else if(sym.isModule) EntityKind.Module
      else if(sym.isMethod) EntityKind.Method
      else if(sym.isVariable) EntityKind.Mutable
      else EntityKind.Term
    }
    Entity(sym.nameString, kind)
  }
}

case class FullDependency(from: Path, to: Path, kind: DependencyKind, count: Int) {
  override def toString = {
    val s = s"$from ${kind.toString.toLowerCase} $to"
    if(count == 1) s else s"$s [$count]"
  }
}

trait Node {
  def path: Path
  def edgesIn: Iterable[Edge]
  def edgesOut: Iterable[Edge]
  def connectTo(to: Node, kind: DependencyKind, count: Int): Edge
  def remove: Boolean
}

trait Edge {
  def from: Node
  def to: Node
  def kind: DependencyKind
  def remove: Boolean
  def count: Int
  override def toString = s"$from -[$kind]-> $to"
}

class Graph(val name: String) { graph =>
  private val nodesMap = mutable.Map[Path, Node]()
  private val edgesSet = mutable.Set[Edge]()

  def nodes: Iterable[Node] = nodesMap.values
  def edges: Iterable[Edge] = edgesSet

  def addNode(path: Path): Node = nodesMap.getOrElseUpdate(path, new GraphNode(path))

  private[this] class GraphNode(val path: Path) extends Node { self =>
    val in = mutable.Map[(Node, DependencyKind), Edge]()
    val out = mutable.Map[(Node, DependencyKind), Edge]()
    var dead = false
    def ensureNotDead[T](v: => T): T =
      if(dead) throw new IllegalStateException(s"Node '$this' has been removed from the graph")
      else v

    def edgesIn: Iterable[Edge] = ensureNotDead(in.values)
    def edgesOut: Iterable[Edge] = ensureNotDead(out.values)

    def connectTo(to: Node, kind: DependencyKind, count: Int): Edge = {
      val _to = to.asInstanceOf[GraphNode]
      ensureNotDead(out.getOrElseUpdate((_to, kind), {
        val e = new GraphEdge(this, _to, kind, count)
        out.put((to, kind), e)
        _to.in.put((self, kind), e)
        graph.edgesSet.add(e)
        e
      }))
    }

    def remove: Boolean = {
      if(dead) false else {
        edgesIn.foreach(_.remove)
        edgesOut.foreach(_.remove)
        graph.nodesMap.remove(path)
        dead = true
        true
      }
    }

    override def toString = path.toString
  }

  /** Remove all nodes (and their connecting edges) whose path matches one of the
    * specified simple path names (i.e. kinds are ignored, names concatenated by '.',
    * no quotations). Descendants of the specified paths are also removed. */
  def removePaths(simplePaths: String*): Unit = {
    val s = simplePaths.toSet
    nodes.toSeq.foreach { n =>
      val simple = n.path.simpleString
      if(s.exists { p =>
        simple == p || simple.startsWith(p + ".")
      }) n.remove
    }
  }

  private[this] class GraphEdge(_from: GraphNode, _to: GraphNode, _kind: DependencyKind, _count: Int) extends Edge {
    var dead = false
    def ensureNotDead[T](v: T): T =
      if(dead) throw new IllegalStateException(s"Edge '$this' has been removed from the graph")
      else v
    def from: Node = ensureNotDead(_from)
    def to: Node = ensureNotDead(_to)
    def kind = ensureNotDead(_kind)
    def count = ensureNotDead(_count)
    def remove: Boolean =
      if(dead) false else {
        _from.out.remove((_to, _kind))
        _to.in.remove((_from, kind))
        graph.edgesSet.remove(this)
        dead = true
        true
      }
  }

  override def toString: String = s"Graph '$name': ${nodes.size} nodes, ${edges.size} edges"

  /** Create a full dump of the graph */
  def fullString: String = {
    val b = new StringBuilder()
    b.append(toString + "\nNodes:\n")
    for(n <- nodes) b.append(s"  - $n\n")
    b.append("Edges:")
    for(e <- edges) b.append(s"\n  - $e")
    b.result()
  }

  def toJsonModel: Seq[FullDependency] =
    edgesSet.map(e => FullDependency(e.from.path, e.to.path, e.kind, e.count)).toSeq.sortBy(_.toString)
}

object Graph {
  def apply(name: String, model: Seq[FullDependency]): Graph = {
    val g = new Graph(name)
    for(d <- model)
      g.addNode(d.from).connectTo(g.addNode(d.to), d.kind, d.count)
    g
  }
}
