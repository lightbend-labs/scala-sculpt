// Copyright (C) 2015-2021 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.model

import scala.collection.mutable

// abstract Node and Edge traits are agnostic about whether the
// underlying data structures are mutable or immutable

trait Node {
  def path: Path
  def edgesIn: Iterable[Edge]
  def edgesOut: Iterable[Edge]
  def connectTo(to: Node, kind: DependencyKind, count: Int): Edge
  def remove(): Boolean
}

trait Edge {
  def from: Node
  def to: Node
  def kind: DependencyKind
  def remove(): Boolean
  def count: Int
  override def toString = s"$from -[$kind]-> $to"
}

// the rest of the code in this file is a particular implementation
// of Node and Edge built on mutable data structures

class Graph(val name: String) { graph =>

  // use LinkedHash* for deterministic ordering, for ease of testing
  private val nodesMap = mutable.LinkedHashMap[Path, Node]()
  private val edgesSet = mutable.LinkedHashSet[Edge]()

  def nodes: Iterable[Node] = nodesMap.values
  def edges: Iterable[Edge] = edgesSet

  def addNode(path: Path): Node = nodesMap.getOrElseUpdate(path, new GraphNode(path))

  private[this] class GraphNode(val path: Path) extends Node { self =>
    val in = mutable.LinkedHashMap[(Node, DependencyKind), Edge]()
    val out = mutable.LinkedHashMap[(Node, DependencyKind), Edge]()
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

    def remove(): Boolean = {
      if(dead) false else {
        edgesIn.foreach(_.remove())
        edgesOut.foreach(_.remove())
        graph.nodesMap.remove(path)
        dead = true
        true
      }
    }

    override def toString = path.toString
  }

  /** Remove all nodes (and their connecting edges) whose path matches one of the
    * specified simple path names (i.e. kinds are ignored, names concatenated by '.',
    * no quotations). Descendents of the specified paths are also removed. */
  def removePaths(simplePaths: String*): Unit = {
    val s = simplePaths.toSet
    // first remove matching nodes
    nodes.foreach { n =>
      val name = n.path.nameString
      if(s.exists { p =>
        name == p || name.startsWith(p + ".")
      }) n.remove()
    }
    // after the first round of removals, we may now have "orphan" nodes
    // with no incoming or outgoing edges. we'll remove those too
    nodes.foreach { n =>
      if (n.edgesIn.isEmpty && n.edgesOut.isEmpty)
        n.remove()
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
    def remove(): Boolean =
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
