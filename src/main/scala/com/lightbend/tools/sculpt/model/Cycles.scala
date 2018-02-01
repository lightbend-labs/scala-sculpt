// Copyright (C) 2015-2018 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.model

object Cycles {

  // input type
  type Nodes = Iterable[Node]

  // output types
  type Cycle = Set[Node]
  type Layer = Vector[Set[Node]]

  /** Cycles in the graph, in (a not-uniquely-determined) reverse topological order.
    * The ordering property means that edges never exist from earlier-listed to
    * later-listed cycles.  Even one-node "cycles" are included in the output. */
  def cycles(nodes: Nodes): Vector[Cycle] =
    (new Components)(nodes)(_.edgesOut.map(_.to))

  /** Human-readable report of cycles (size > 1) in the graph, in descending
    * order by size. */
  def cyclesString(nodes: Nodes): String =
    cycles(nodes)
      .sortBy(-_.size)
      .takeWhile(_.size > 1)
      .map(cycle => s"[${cycle.size}] ${cycleString(cycle)}")
      .mkString("\n")

  private def cycleString(cycle: Cycle): String =
    cycle.toSeq.map(_.path.simpleString).sortBy(_.toString).mkString(" ")

  /** Layers in the graph, in ascending order */
  def layers(nodes: Nodes): Vector[Layer] = {
    def recurse(seen: Set[Node], cycles: Vector[Cycle]): Vector[Layer] =
      if (cycles.isEmpty)
        Vector()
      else {
        val (layer, others) =
          cycles.partition{cycle =>
            cycle.forall{n1 =>
              n1.edgesOut.map(_.to).forall{n2 => cycle(n2) || seen(n2)}}}
        layer +: recurse(seen ++ layer.flatten, others)
      }
    recurse(Set(), cycles(nodes))
  }

  /** Human-readable report of layers in graph, in descending order. */
  def layersString(nodes: Nodes): String =
    layers(nodes)
      .zipWithIndex
      .reverse
      .map{case (layer, n) =>
        layer.map(cycleString)
          .filter(_.nonEmpty) // omit empty package
          .map(s => s"[$n] $s\n")
          .sorted
          .mkString}
      .mkString

  def path(from: Node, to: Node): Option[Seq[Node]] = {
    type Result = (Set[Node], Option[Seq[Node]])
    def findPath(current: Node, currentPath: List[Node], visitedNodes: Set[Node]): Result =
      if (current == to) Set.empty -> Some((current :: currentPath).reverse)
      else {
        val newVisited = visitedNodes + current
        val targets = current.edgesOut.map(_.to)
        val newPath = current :: currentPath
        targets.foldLeft((newVisited, None): Result) { (res, next) =>
          res match {
            case res@(_, Some(_)) => res
            case res@(newVisited, None) =>
              if (!newVisited(next))
                findPath(next, newPath, newVisited)
              else
                res
          }
        }
      }

    findPath(from, Nil, Set.empty)._2
  }
  def path(nodes: Nodes, from: String, to: String): Option[Seq[Node]] = {
    def get(name: String): Node =
      nodes.find(_.path.nameString.endsWith(name)).getOrElse(throw new NoSuchElementException(name))

    path(get(from), get(to))
  }

  def pp(nodes: Nodes, from: String, to: String): Unit = {
    println("Forward")
    path(nodes, from ,to).foreach(_.foreach(n => println(n.path.simpleString)))
    println("Backward")
    path(nodes, to,from).foreach(_.foreach(n => println(n.path.simpleString)))
  }

  def dot(graph: Graph): String = {
    def nodeFilter(node: Node): Boolean =
      node.path.simpleString.contains(":akka.http")
      /*!node.path.simpleString.contains(":scala") &&
      !node.path.simpleString.contains(":java") &&
      !node.path.simpleString.contains(":akka.stream") &&
      !node.path.simpleString.contains(":akka.util") &&
      !node.path.simpleString.contains(":akka.parboiled2") &&
      !node.path.simpleString.contains(":akka.actor")*/

    def edgeFilter(edge: Edge): Boolean =
      nodeFilter(edge.from) && nodeFilter(edge.to)

    def formatNode(node: Node): String = s""""${node.path.simpleString}""""
    def formatEdge(edge: Edge): String = s""""${edge.from.path.simpleString}"->"${edge.to.path.simpleString}""""

    s"""|digraph "dependency-graph" {
       |    graph[rankdir="LR"]
       |    edge [
       |        arrowtail="none"
       |    ]
       |    ${graph.nodes.iterator.filter(nodeFilter).map(formatNode).mkString("\n    ")}
       |    ${graph.edges.iterator.filter(edgeFilter).map(formatEdge).mkString("\n    ")}
       |}
       |""".stripMargin
  }
}
