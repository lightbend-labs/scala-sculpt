// Copyright (C) 2015-2020 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.model

// implements Tarjan's strongly connected components algorithm; see
// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

// parameterized on the node type, so the whole thing can be as abstract as possible.
// `<: AnyRef` is an efficiency hack: it lets use `ne` instead of `!=`

class Components[T <: AnyRef] {

  def apply(nodes: Iterable[T])(successors: T => Iterable[T]): Vector[Set[T]] = {

    val components = Vector.newBuilder[Set[T]]
    val indexer = new Indexer
    val stack = new Stack
    val lowestReachable = new Minimizer

    def recurse(node: T): Unit = {
      lowestReachable.update(node, indexer.tag(node))
      stack.push(node)
      for (node2 <- successors(node))
        if (!indexer.contains(node2)) {
          recurse(node2)
          lowestReachable.update(node, lowestReachable(node2))
        }
        else if (stack.contains(node2))
          lowestReachable.update(node, indexer(node2))
      if (indexer(node) == lowestReachable(node))
        components += stack.popUntil(node)
    }

    for (node <- nodes)
      if (!indexer.contains(node))
        recurse(node)

    components.result()

  }

  // remembers lowest number seen for each item
  private class Minimizer {
    private val mins = collection.mutable.Map.empty[T, Int]
    def update(x: T, n: Int): Unit =
      if (mins.contains(x))
        mins(x) = mins(x) min n
      else
        mins(x) = n
    def apply(x: T): Int =
      mins(x)
  }

  // assigns successive numbers to items; queryable
  private class Indexer {
    private val n = Iterator.from(0)
    private val indices = collection.mutable.Map.empty[T, Int]
    def tag(x: T): Int = {
      val next = n.next()
      indices(x) = next
      next
    }
    def apply(x: T): Int =
      indices(x)
    def contains(x: T): Boolean =
      indices.contains(x)
  }

  // stack plus set, so we can check for membership cheaply
  private class Stack {
    private val stack = collection.mutable.Stack.empty[T]
    private val set = collection.mutable.Set.empty[T]
    def contains(x: T): Boolean =
      set(x)
    def push(x: T): Unit = {
      stack.push(x)
      set += x
    }
    def pop(): T = {
      val result = stack.pop()
      set -= result
      result
    }
    // pop stack til we hit x;
    // return popped values including x itself
    def popUntil(x: T): Set[T] =
      (Iterator.continually(pop())
        .takeWhile(_ ne x)
        .toSet) + x
  }

}
