# sclib - simple scala utility library

  - stdlib extensions
  - toolbox to work with files and directories
  - utilities
  - zero runtime dependencies
  - jvm and scala.js bits
  - [scaladoc](http://j-keck.github.io/sclib/latest/api/)

## quick start 

the library are published to bintray for scala 2.10 and 2.11.
add the following snippet to your `build.sbt` file:

<sbt-snippet>
- for the jvm:

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %% "sclib" % "0.6"

- for scala.js

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %%% "sclib" % "0.6"
</sbt-snippet>

## content

   - [stdlib extensions](#stdlib-extensions)
     - [Either](#either)
     - [Try](#try)
     - [Option](#option)
     - [List](#list)
     - [String](#string)
     - [Java8 interoperability](#java8-interoperability)
   - [io](#io)
   - [util](#util)
   - [patterns](#patterns)
   - [(very) simple serialize / deserialize](#very-simple-serialize--deserialize)   


### stdlib extensions

utilities for the scala stdlib.

to import all in one use `import sclib.ops.all._`

#### Either
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.either)

```tut:silent:reset
import sclib.ops.either._
```

  - shorthand Left / Right constructor:
```tut
"a string".left
"a string".left[Int] 
4.right[String]
```

  - sequence on `Traversable[Either[A, B]]` to reducing many `Either`s into a single `Either`
```tut
List(3.right, 4.right).sequence
List(3.right, 4.right, "BOOM".left).sequence
Vector(2.right, 5.right).sequence
```
   
  - right biased either
```tut
for {
  a <- Right(1)
  b <- Right(4)
} yield a + b
```

#### Try
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.try)

```tut:silent:reset
import sclib.ops.`try`._
```

  - shorthand constructor for `Success`
```tut
3.success
```
  
  - shorthand constructor for `Failure` from a `Throwable`
```tut
new IllegalArgumentException("BOOM").failure[Int]
```

  - shorthand constructor for `Failure` from a `String`
```tut
"BOOM".failure
```

  - sequence on `Traversable[Try[A]]` to reducing many `Try`s into a single `Try`
```tut
List(3.success, 44.success).sequence
List(3.success, "BOOM".failure, 44.success).sequence
Vector(1.success, 2.success).sequence
```

#### Option
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.option)

```tut:silent:reset
import sclib.ops.option._
```

  - shorthand constructor for `Some` (with type `Option[A]`)
```tut
123.some
```

  - shorthand constructor for `None` (with type `Option[A]`)
  
```tut
none
none[String]
```

  - sequence on `Traversable[Option[A]]` to reducing many `Option`s into a single `Option`
```tut
List(3.some, 44.some).sequence
List(3.some, none, 44.some).sequence
Vector(1.some, 2.some).sequence
```

#### List
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.list)

```tut:silent:reset
import sclib.ops.list._
```

  - unfoldLeft / unfoldRight
```tut
ListOps.unfoldRight(0){ i =>
  if(i > 10) None else Some((i, i + 1))
}
```

  - partition a list into sub-lists - start by the given predicate
```tut
val l = List("-- heading1", "a", "b", "-- heading2", "c", "d")
l.partitionsBy(_.startsWith("--"))
```

#### String
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.string)

```tut:silent:reset
import sclib.ops.string._
```

  - save / easy to compose parser for `Int`, `Long`, `Double`, `Char`, `Boolean` and `Date`.
    the result can be wrapped in a `Try`, `Option`, `Either[String, ?]` or `Either[Throwable, ?]`.
```tut
import scala.util.Try
"123".parseInt[Try]
"one".parseInt[Try]

"123".parseInt[Option]
"one".parseInt[Option]

"123".parseInt[Either[String, ?]]
"one".parseInt[Either[String, ?]]

import sclib.ops.either._
for{
 a <- "123".parseInt[Try]
 b <- "44".parseInt[Try]
} yield a + b

for{
 a <- "one".parseInt[Try]
 b <- "44".parseInt[Try]
} yield a + b
```

  - parseDate[F[_]](pattern: String)
```tut  
"Feb 01 16:30:10 2020".parseDate[Try]("MMM DD HH:mm:ss yyyy")
```

  - parseDate[F[_]]
which expects a implicit `SimpleDateFormat` in scope
```tut:silent
implicit val sdf = new java.text.SimpleDateFormat("DD.MM.yyyy HH:mm:ss")
```
```tut
"01.02.2020 16:30:10".parseDate[Try]
```


#### java8 interoperability
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.java8)

```tut:silent:reset
import sclib.ops.java8._
```

  - convert a `java.util.stream.Stream` to 'scala.collection.Iterator'
```tut
java.util.stream.Stream.of(1, 2, 3, 4).toIterator
```
  - convert a `java.util.stream.Stream` to 'scala.collection.immutable.List'
```tut
java.util.stream.Stream.of(1, 2, 3, 4).toList
```
  - convert a `scala.Function1` to a `java.util.function.Function`
```tut
java.util.stream.Stream.of(1, 2, 3,4).map((_: Int) * 10).toArray
```
  - convert a `scala.Function1` to a `java.util.function.Predicate`
```tut
java.util.stream.Stream.of(1, 2, 3, 4).filter((_: Int) < 3).toArray
```
  - convert a `scala.Function1` to a `java.util.function.Consumer`
```tut
java.util.stream.Stream.of(1, 2, 3, 4).forEach(println(_: Int))
```
  - convert a `scala.Function2` to a `java.util.function.BinaryOperator`
```tut
java.util.stream.Stream.of(1, 2, 3).reduce(0, (_: Int) + (_: Int))
```


### io
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.io.package)

- simple version of java's 'try-with-resource'
```scala
import sclib.io.autoClose
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.{CREATE, WRITE}
import java.nio.channels.FileChannel
for {
  in <- autoClose(FileChannel.open(Paths.get("/tmp/input")))
  out <- autoClose(FileChannel.open(Paths.get("/tmp/output"), CREATE, WRITE))
} in.transferTo(0, Long.MaxValue, out)
// `in` and `out` are closed here
```

#### filesystem
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.io.fs.package)
```tut:silent:reset
import sclib.io.fs._
```
functions to work with files and directories.

- all functions which can throw a exception are wrapped in a `Try`.
```tut
for {
  wd <- dir("sclib-example")
  wd <- wd.createTemp              // create a temp work-dir (path is something like: '/tmp/sclib-example6964564891871111476')
  fh <- file(wd, "a-file")         // create a file under the work-dir
  _ <- fh.append("first line\n")   // write a line
  _ <- fh.append("second line")
  fs <- fh.size                    // file-size  (fh.size returns Try[Long])
  lc <- fh.lines.map(_.length)     // line-count (fh.lines returns Try[Iterator[String]])
  wc <- fh.slurp.map(_.size)       // word-count (fh.slurp returns Try[String])
  _ <- wd.deleteR                  // delete the work-dir recursive
} yield s"file size: ${fs}, line count: ${lc}, word count: ${wc}"
```

- type-class based `write`, `writeLines`, `append` and `appendLines` functions with instances for [basic types](http://j-keck.github.io/sclib/latest/api/#sclib.io.fs.Writable$)
```tut
for {
  wd <- dir("sclib-example")
  wd <- wd.createTemp                                   // create a temp work-dir (path is something like: '/tmp/sclib-example6964564891871111476')
  fh <- file(wd, "a-file")                              // create a file under the work-dir
  _ <- fh.writeLines("1. apple")                        // string
  _ <- fh.appendLines(List("2. banana", "3. cherry"))   // list of string
  _ <- fh.append(4)                                     // int
  _ <- fh.append('.')                                   // char
  _ <- fh.append(Vector(' ', 'd', 'o', 'g'))            // vector of char
  content <- fh.slurp                                   // read the whole file 
  _ <- wd.deleteR                                       // delete the work-dir recursive
} yield content
```

#### net 
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.io.net.package)
```tut:silent:reset
import sclib.io.net._
```
 
 - download a file
```scala
scala> import sclib.io.fs._
scala> for {
     |    url <- url("http://example.com")         // save way to get a 'java.net.URL'
     |    local <- url.fetch(file("example.com"))  // download the url-content
     |    content <- local.slurp                   // read the local copy
     |    _ <- local.delete()                      // delete the local copy
     |  } yield content.take(20)
res0: scala.util.Try[String] = Success(<!doctype html>
      <htm)
```

### util
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.io.util.package)
```tut:silent:reset
```

- simple Union type
```tut
import sclib.util.union._
def f[A: (Int Or String)#Check](a: A): Int = a match {
  case i: Int => i
  case s: String => s.length
}
f(5)
f("hey")

// this throws a compiler error:
// f(5L) 
```



### "pattern's"
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.patterns.package)

```tut:silent:reset
import sclib.patterns._
```

#### AppF

simple *AppF*unction - expect's a function with receives a config and returns a Either

```tut
import sclib.ops.either._

val action = for {
  a <- AppF{i: Int => i.right[String]}
  b <- AppF{i: Int => if(i < 5) (i * 10).right else "BOOM".left}
  c <- AppF.lift(33.right[String])
} yield (a, b, c)

action.runEitherT.runReader(2)

action.runEitherT.runReader(8)
```

### (very) simple serialize / deserialize
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.serialization.simple.package)

i use this for little scala.js apps for the client / server communication
to keep the resulting '.js' files small.

values are converted to strings and prefixed with their length.
so the int value 31593 becomes "5:31593". to deserialize this value,
we give the deserializer the expected type, and it parse / converts the given string.


```tut:silent:reset
import sclib.serialization.simple._
```

#####for stdlib
  
```tut
val s = Serialize("a simple string")
Deserialize[String](s)

val t = Serialize("a tuple with a string and a list" -> List(4, 23, 1))
Deserialize[(String, List[Int])](t)
```

#####for own types
  
  - define your types and the typeclass instances for serialization / deserialization
  
```tut:silent
case class C(a: String, b: List[Int], c: Either[Int, String])

implicit val cSer = new Serialize[C]{
  override def apply(cInst: C): String = {
    import cInst._
    Serialize(a) + Serialize(b) + Serialize(c)
  }
}

implicit val cDes = new Deserialize[C]{
  override def apply: DeserializeState[C] = for {
    a <- Deserialize[String]
    b <- Deserialize[List[Int]]
    c <- Deserialize[Either[Int, String]]
  } yield C(a, b, c)
}
```

  - use it
```tut
val s = Serialize(C("the string", List(4, 2, 1), Right("i'm ok")))
Deserialize[C](s)
```
