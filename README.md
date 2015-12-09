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
res0: Seq[scala.tools.sculpt.model.FullDependency] = List((class)Dep1 extends (package)scala.(type)AnyRef [1], (class)Dep1.(constructor)<init> uses (class)Dep1 [1], (class)Dep1.(constructor)<init> uses (package)java.(package)lang.(class)Object.(constructor)<init> [1], (class)Dep1.(val)x uses (package)scala.(class)Int [2], (class)Dep2 extends (package)scala.(type)AnyRef [1], (class)Dep2.(constructor)<init> uses (class)Dep2 [1], (class)Dep2.(constructor)<init> uses (package)java.(package)lang.(class)Object.(constructor)<init> [1], (class)Dep2.(val)x uses (package)scala.(class)Int [2])

scala>
```
