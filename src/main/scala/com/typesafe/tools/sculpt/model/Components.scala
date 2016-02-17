// Copyright (C) 2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt.model

// implements Tarjan's strongly connected components algorithm; see
// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
// code is a line-by-line transliteration of the pseudocode into Scala

object Components {

  // TODO take just the nodes and edges iterables, instead of the Graph object?

  def apply(g: Graph): Seq[Set[Node]] = {

    val components = collection.mutable.Buffer.empty[Set[Node]]

    var index = 0
    val stack = collection.mutable.Stack.empty[Node]
    val indices = collection.mutable.Map.empty[Node, Int]
    val lowLinks = collection.mutable.Map.empty[Node, Int]
    val onStack = collection.mutable.Set.empty[Node]

    def strongConnect(v: Node): Unit = {

      val component = collection.mutable.Set.empty[Node]

      // Set the depth index for v to the smallest unused index
      indices(v) = index
      lowLinks(v) = index
      index += 1
      stack.push(v)
      onStack += v

      // Consider successors of v
      for (w <- v.edgesOut.map(_.to))
        if (!indices.contains(w)) {
          // Successor w has not yet been visited; recurse on it
          strongConnect(w)
          lowLinks(v) = lowLinks(v) min lowLinks(w)
        }
        else if (onStack(w))
          // Successor w is in stack S and hence in the current SCC
          lowLinks(v) = lowLinks(v) min indices(w)

      // If v is a root node, pop the stack and generate an SCC
      if (lowLinks(v) == indices(v)) {
        // start a new strongly connected component
        var w: Node = null
        do {
          w = stack.pop()
          onStack -= w
          component += w
        } while (w ne v);
        components += component.toSet
      }
    }

    for (v <- g.nodes)
      if (!indices.contains(v))
        strongConnect(v)

    components.toSeq

  }
}
