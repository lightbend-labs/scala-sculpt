# Sculpt: dependency graph extraction for Scala

## Building from source

`sbt package` will create `target/scala-2.11/scala-sculpt_2.11-0.0.1.jar`.

## Using the compiler plugin

You can use the compiled plugin with the Scala 2.11 compiler as follows.

First, make sure you have `scala-sculpt_2.11-0.0.1.jar` in your current working directory,
along with `spray-json_2.11-1.3.2.jar` (which you can download
[here](http://repo1.maven.org/maven2/io/spray/spray-json_2.11/1.3.2/spray-json_2.11-1.3.2.jar).

Then you can do e.g.:

    scalac -Xplugin:scala-sculpt_2.11-0.0.1.jar:spray-json_2.11-1.3.2.jar \
      -Xplugin-require:sculpt \
      -P:sculpt:out=dep.json \
      Dep.scala

## Sample input and output

Assuming `Dep.scala` contains this source code:

```
object Dep1 { final val x = 42 }
object Dep2 { val x = Dep1.x }
```

then the command line shown above will generate this `dep.json` file:

```
[
  {"sym": ["o:Dep1"], "extends": ["pkt:scala", "tp:AnyRef"]},
  {"sym": ["o:Dep1", "def:<init>"], "uses": ["o:Dep1"]},
  {"sym": ["o:Dep1", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
  {"sym": ["o:Dep1", "def:x"], "uses": ["pkt:scala", "cl:Int"]},
  {"sym": ["o:Dep1", "t:x"], "uses": ["pkt:scala", "cl:Int"]},
  {"sym": ["o:Dep2"], "extends": ["pkt:scala", "tp:AnyRef"]},
  {"sym": ["o:Dep2", "def:<init>"], "uses": ["o:Dep2"]},
  {"sym": ["o:Dep2", "def:<init>"], "uses": ["pkt:java", "pkt:lang", "cl:Object", "def:<init>"]},
  {"sym": ["o:Dep2", "def:x"], "uses": ["o:Dep2", "t:x"]},
  {"sym": ["o:Dep2", "def:x"], "uses": ["pkt:scala", "cl:Int"]},
  {"sym": ["o:Dep2", "t:x"], "uses": ["pkt:scala", "cl:Int"]}
]
```

For brevity, the following abbreviations are used in the JSON output:

### Terms

abbreviation | meaning
-------------|--------
ov           | object
def          | def
var          | var
mac          | macro
pk           | package
t            | other term

### Types

abbreviation | meaning
-------------|--------
tr           | trait
pkt          | package
o            | object
cl           | class
tp           | other type

## Sample interactive session

Now in a Scala 2.11 REPL with the same JARs on the classpath:

    scala -classpath scala-sculpt_2.11-0.0.1.jar:spray-json_2.11-1.3.2.jar

If we load `dep.json` as follows, we'll see the following graph:

```
scala> import scala.tools.sculpt.cmd._
import scala.tools.sculpt.cmd._

scala> load("dep.json")
res0: scala.tools.sculpt.model.Graph = Graph 'dep.json': 11 nodes, 11 edges

scala> println(res0.fullString)
Graph 'dep.json': 11 nodes, 11 edges
Nodes:
  - o:Dep1
  - pkt:scala.tp:AnyRef
  - o:Dep1.def:<init>
  - pkt:java.pkt:lang.cl:Object.def:<init>
  - o:Dep1.def:x
  - pkt:scala.cl:Int
  - o:Dep1.t:x
  - o:Dep2
  - o:Dep2.def:<init>
  - o:Dep2.def:x
  - o:Dep2.t:x
Edges:
  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep1.def:<init> -[Uses]-> o:Dep1
  - o:Dep1.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep2 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep2.def:<init> -[Uses]-> o:Dep2
  - o:Dep2.def:<init> -[Uses]-> pkt:java.pkt:lang.cl:Object.def:<init>
  - o:Dep2.def:x -[Uses]-> o:Dep2.t:x
  - o:Dep2.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep2.t:x -[Uses]-> pkt:scala.cl:Int
```

and we can explore the effect of removing edges from the graph using `removePaths`:

```
scala> res0.removePaths("Dep2", "java.lang")

scala> println(res0.fullString)
Graph 'dep.json': 6 nodes, 4 edges
Nodes:
  - o:Dep1
  - pkt:scala.tp:AnyRef
  - o:Dep1.def:<init>
  - o:Dep1.def:x
  - pkt:scala.cl:Int
  - o:Dep1.t:x
Edges:
  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep1.def:<init> -[Uses]-> o:Dep1
  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
```

Saving the graph back to a JSON model and loading it again:

```
scala> save(res0, "dep2.json")

scala> load("dep2.json")
res5: scala.tools.sculpt.model.Graph = Graph 'dep2.json': 3 nodes, 2 edges

scala> println(res5.fullString)
Graph 'dep2.json': 6 nodes, 4 edges
Nodes:
  - o:Dep1
  - pkt:scala.tp:AnyRef
  - o:Dep1.def:<init>
  - o:Dep1.def:x
  - pkt:scala.cl:Int
  - o:Dep1.t:x
Edges:
  - o:Dep1 -[Extends]-> pkt:scala.tp:AnyRef
  - o:Dep1.def:<init> -[Uses]-> o:Dep1
  - o:Dep1.def:x -[Uses]-> pkt:scala.cl:Int
  - o:Dep1.t:x -[Uses]-> pkt:scala.cl:Int
```
