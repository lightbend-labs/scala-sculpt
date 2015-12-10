package scala.tools.sculpt.model

object TreePrinter {

  def apply(graph: Graph): String = {
    val sb = new StringBuilder
    sb ++= s"${graph.name}:\n"
    def traverse(node: Node, prefix: String = ""): Unit = {
      sb ++= prefix
      sb ++= s"└── ${node.path.elems.map(_.name).mkString(".")}\n"
      for (edge <- node.edgesOut)
        traverse(edge.to, prefix + "    ")
    }
    for {
      node <- graph.nodes
      kind = node.path.elems.last.kind
      if kind.isInstanceOf[EntityKind.AnyType]
    } traverse(node)
    sb.toString
  }

}
