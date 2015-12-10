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

```
scala> import scala.tools.sculpt.cmd._
import scala.tools.sculpt.cmd._

scala> load("../stest/dep.json")
res0: scala.tools.sculpt.model.Graph =
Graph '../stest/dep.json': 9 nodes, 8 edges
Nodes:
  - pck:scala.t:AnyRef
  - cl:Dep2
  - cl:Dep1.val:x
  - cl:Dep2.val:x
  - pck:scala.cl:Int
  - cl:Dep1
  - cl:Dep1.cons:<init>
  - cl:Dep2.cons:<init>
  - pck:java.pck:lang.cl:Object.cons:<init>
Edges:
  - cl:Dep1.cons:<init> -[Uses]-> cl:Dep1
  - cl:Dep1.cons:<init> -[Uses]-> pck:java.pck:lang.cl:Object.cons:<init>
  - cl:Dep2.cons:<init> -[Uses]-> cl:Dep2
  - cl:Dep1.val:x -[Uses]-> pck:scala.cl:Int
  - cl:Dep2 -[Extends]-> pck:scala.t:AnyRef
  - cl:Dep1 -[Extends]-> pck:scala.t:AnyRef
  - cl:Dep2.cons:<init> -[Uses]-> pck:java.pck:lang.cl:Object.cons:<init>
  - cl:Dep2.val:x -[Uses]-> pck:scala.cl:Int

scala>
```
