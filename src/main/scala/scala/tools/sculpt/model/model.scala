package scala.tools.sculpt.model

import scala.StringBuilder
import scala.collection.mutable
import scala.reflect.internal.Symbols

sealed abstract class EntityKind(val prefix: String)
object EntityKind {
  case object Trait extends EntityKind("tr")
  case object Class extends EntityKind("cl")
  case object Type extends EntityKind("t")
  case object Var extends EntityKind("var")
  case object Package extends EntityKind("pck")
  case object Object extends EntityKind("o")
  case object Constructor extends EntityKind("cons")
  case object Def extends EntityKind("def")
  case object Val extends EntityKind("val")
  case object Unknown extends EntityKind("u")
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

case class FullDependency(from: Path, to: Path, kind: DependencyKind, count: Int) {
  override def toString = {
    val s = s"$from ${kind.toString.toLowerCase} $to"
    if(count == 1) s else s"$s [$count]"
  }
}

trait Node {
  def edgesIn: Iterable[Edge]
  def edgesOut: Iterable[Edge]
  def connectTo(to: Node, kind: DependencyKind): Edge
  def remove: Boolean
}

trait Edge {
  def from: Node
  def to: Node
  def kind: DependencyKind
  def remove: Boolean
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

    def connectTo(to: Node, kind: DependencyKind): Edge = {
      val _to = to.asInstanceOf[GraphNode]
      ensureNotDead(out.getOrElseUpdate((_to, kind), {
        val e = new GraphEdge(this, _to, kind)
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

  private[this] class GraphEdge(_from: GraphNode, _to: GraphNode, _kind: DependencyKind) extends Edge {
    var dead = false
    def ensureNotDead[T](v: T): T =
      if(dead) throw new IllegalStateException(s"Edge '$this' has been removed from the graph")
      else v
    def from: Node = ensureNotDead(_from)
    def to: Node = ensureNotDead(_to)
    def kind = ensureNotDead(_kind)
    def remove: Boolean =
      if(dead) false else {
        _from.out.remove((_to, _kind))
        _to.in.remove((_from, kind))
        graph.edgesSet.remove(this)
        dead = true
        true
      }
  }

  override def toString: String = {
    val b = new StringBuilder()
    b.append(s"Graph '$name': ${nodes.size} nodes, ${edges.size} edges\nNodes:\n")
    for(n <- nodes) b.append(s"  - $n\n")
    b.append("Edges:\n")
    for(e <- edges) b.append(s"  - $e\n")
    b.result()
  }
}

object Graph {
  def apply(name: String, model: Seq[FullDependency]): Graph = {
    val g = new Graph(name)
    for(d <- model)
      g.addNode(d.from).connectTo(g.addNode(d.to), d.kind)
    g
  }
}
