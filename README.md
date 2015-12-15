Sculpt - Dependency graph extraction for Scala
==============================================

Using the compiler plugin
-------------------------

After `copyResources` in sbt, you can use the compiled plugin from another scala compiler instance. For example:

    scalac -Xplugin:/Users/szeiger/code/scala-sculpt/target/scala-2.11/classes:\
                    /Users/szeiger/.ivy2/cache/io.spray/spray-json_2.11/bundles/spray-json_2.11-1.3.2.jar \
                    -Xplugin-require:sculpt -P:sculpt:out=dep.json Dep.scala

Sample interactive session
--------------------------

Loading a JSON model into the REPL:

```
scala> import scala.tools.sculpt.cmd._
import scala.tools.sculpt.cmd._

scala> load("../stest/dep.json")
res0: scala.tools.sculpt.model.Graph = Graph '../stest/dep.json': 11 nodes, 11 edges

scala> println(res0.fullString)
Graph '../stest/dep.json': 11 nodes, 11 edges
Nodes:
  - o:Dep1.def:<init>
  - o:Dep2.t:x
  - pkt:scala.cl:Int
  - o:Dep1.def:x
  - o:Dep2
  - pkt:scala.tp:AnyRef
  - o:Dep1.t:x
  - o:Dep2.def:x
  - o:Dep2.def:<init>
  - pkt:java.pkt:lang.cl:Object.def:<init>
  - o:Dep1
Edges:
  - o:Dep2.def:<init> -[Uses]-> o:Dep2
  - o:Dep1.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
  - o:Dep2.def:x -[Uses]-> o:Dep2.t:x
  - o:Dep2.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
  - o:Dep2.t:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep2 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep2.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1.def:<init> -[Uses]-> o:Dep1
```

Removing some nodes:

```
scala> res0.removePaths("Dep2", "java.lang")

scala> println(res0.fullString)
Graph '../stest/dep.json': 6 nodes, 4 edges
Nodes:
  - o:Dep1.def:<init>
  - pkt:scala.cl:Int
  - o:Dep1.def:x
  - pkt:scala.tp:AnyRef
  - o:Dep1.t:x
  - o:Dep1
Edges:
  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1.def:<init> -[Uses]-> o:Dep1
```

Saving the graph back to a JSON model and loading it again:

```
scala> save(res0, "dep2.json")

scala> load("dep2.json")
res5: scala.tools.sculpt.model.Graph = Graph 'dep2.json': 6 nodes, 4 edges

scala> println(res5.fullString)
Graph 'dep2.json': 6 nodes, 4 edges
Nodes:
  - o:Dep1.def:<init>
  - pkt:scala.cl:Int
  - o:Dep1.def:x
  - pkt:scala.tp:AnyRef
  - o:Dep1.t:x
  - o:Dep1
Edges:
  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep1.def:<init> -[Uses]-> o:Dep1
  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
```
