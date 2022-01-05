// Copyright (C) 2015-2022 Lightbend Inc. <http://lightbend.com>

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

}
