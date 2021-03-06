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

```scala
import sclib.ops.either._
```

  - shorthand Left / Right constructor:
```scala
scala> "a string".left
res0: Either[String,Nothing] = Left(a string)

scala> "a string".left[Int] 
res1: Either[String,Int] = Left(a string)

scala> 4.right[String]
res2: Either[String,Int] = Right(4)
```

  - sequence on `Traversable[Either[A, B]]` to reducing many `Either`s into a single `Either`
```scala
scala> List(3.right, 4.right).sequence
res3: Either[Nothing,List[Int]] = Right(List(3, 4))

scala> List(3.right, 4.right, "BOOM".left).sequence
res4: Either[String,List[Int]] = Left(BOOM)

scala> Vector(2.right, 5.right).sequence
res5: Either[Nothing,scala.collection.immutable.Vector[Int]] = Right(Vector(2, 5))
```
   
  - right biased either
```scala
scala> for {
     |   a <- Right(1)
     |   b <- Right(4)
     | } yield a + b
res6: Either[Nothing,Int] = Right(5)
```

#### Try
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.try)

```scala
import sclib.ops.`try`._
```

  - shorthand constructor for `Success`
```scala
scala> 3.success
res0: scala.util.Try[Int] = Success(3)
```
  
  - shorthand constructor for `Failure` from a `Throwable`
```scala
scala> new IllegalArgumentException("BOOM").failure[Int]
res1: scala.util.Try[Int] = Failure(java.lang.IllegalArgumentException: BOOM)
```

  - shorthand constructor for `Failure` from a `String`
```scala
scala> "BOOM".failure
res2: scala.util.Try[Nothing] = Failure(java.lang.Exception: BOOM)
```

  - sequence on `Traversable[Try[A]]` to reducing many `Try`s into a single `Try`
```scala
scala> List(3.success, 44.success).sequence
res3: scala.util.Try[List[Int]] = Success(List(3, 44))

scala> List(3.success, "BOOM".failure, 44.success).sequence
res4: scala.util.Try[List[Int]] = Failure(java.lang.Exception: BOOM)

scala> Vector(1.success, 2.success).sequence
res5: scala.util.Try[scala.collection.immutable.Vector[Int]] = Success(Vector(1, 2))
```

#### Option
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.option)

```scala
import sclib.ops.option._
```

  - shorthand constructor for `Some` (with type `Option[A]`)
```scala
scala> 123.some
res0: Option[Int] = Some(123)
```

  - shorthand constructor for `None` (with type `Option[A]`)
  
```scala
scala> none
res1: Option[Nothing] = None

scala> none[String]
res2: Option[String] = None
```

  - sequence on `Traversable[Option[A]]` to reducing many `Option`s into a single `Option`
```scala
scala> List(3.some, 44.some).sequence
res3: Option[List[Int]] = Some(List(3, 44))

scala> List(3.some, none, 44.some).sequence
res4: Option[List[Int]] = None

scala> Vector(1.some, 2.some).sequence
res5: Option[scala.collection.immutable.Vector[Int]] = Some(Vector(1, 2))
```

#### List
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.list)

```scala
import sclib.ops.list._
```

  - unfoldLeft / unfoldRight
```scala
scala> ListOps.unfoldRight(0){ i =>
     |   if(i > 10) None else Some((i, i + 1))
     | }
res0: List[Int] = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

  - partition a list into sub-lists - start by the given predicate
```scala
scala> val l = List("-- heading1", "a", "b", "-- heading2", "c", "d")
l: List[String] = List(-- heading1, a, b, -- heading2, c, d)

scala> l.partitionsBy(_.startsWith("--"))
res1: List[List[String]] = List(List(-- heading1, a, b), List(-- heading2, c, d))
```

#### String
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.string)

```scala
import sclib.ops.string._
```

  - save / easy to compose parser for `Int`, `Long`, `Double`, `Char`, `Boolean` and `Date`.
    the result can be wrapped in a `Try`, `Option`, `Either[String, ?]` or `Either[Throwable, ?]`.
```scala
scala> import scala.util.Try
import scala.util.Try

scala> "123".parseInt[Try]
res0: scala.util.Try[Int] = Success(123)

scala> "one".parseInt[Try]
res1: scala.util.Try[Int] = Failure(java.lang.NumberFormatException: For input string: "one")

scala> "123".parseInt[Option]
res2: Option[Int] = Some(123)

scala> "one".parseInt[Option]
res3: Option[Int] = None

scala> "123".parseInt[Either[String, ?]]
res4: scala.util.Either[String,Int] = Right(123)

scala> "one".parseInt[Either[String, ?]]
res5: scala.util.Either[String,Int] = Left(java.lang.NumberFormatException: For input string: "one")

scala> import sclib.ops.either._
import sclib.ops.either._

scala> for{
     |  a <- "123".parseInt[Try]
     |  b <- "44".parseInt[Try]
     | } yield a + b
res6: scala.util.Try[Int] = Success(167)

scala> for{
     |  a <- "one".parseInt[Try]
     |  b <- "44".parseInt[Try]
     | } yield a + b
res7: scala.util.Try[Int] = Failure(java.lang.NumberFormatException: For input string: "one")
```

  - parseDate[F[_]](pattern: String)
```scala
scala> "Feb 01 16:30:10 2020".parseDate[Try]("MMM DD HH:mm:ss yyyy")
res8: scala.util.Try[java.util.Date] = Success(Wed Jan 01 16:30:10 CET 2020)
```

  - parseDate[F[_]]
which expects a implicit `SimpleDateFormat` in scope
```scala
implicit val sdf = new java.text.SimpleDateFormat("DD.MM.yyyy HH:mm:ss")
```
```scala
scala> "01.02.2020 16:30:10".parseDate[Try]
res9: scala.util.Try[java.util.Date] = Success(Wed Jan 01 16:30:10 CET 2020)
```


#### java8 interoperability
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.java8)

```scala
import sclib.ops.java8._
```

  - convert a `java.util.stream.Stream` to 'scala.collection.Iterator'
```scala
scala> java.util.stream.Stream.of(1, 2, 3, 4).toIterator
res0: Iterator[Int] = non-empty iterator
```
  - convert a `java.util.stream.Stream` to 'scala.collection.immutable.List'
```scala
scala> java.util.stream.Stream.of(1, 2, 3, 4).toList
res1: List[Int] = List(1, 2, 3, 4)
```
  - convert a `scala.Function1` to a `java.util.function.Function`
```scala
scala> java.util.stream.Stream.of(1, 2, 3,4).map((_: Int) * 10).toArray
res2: Array[Object] = Array(10, 20, 30, 40)
```
  - convert a `scala.Function1` to a `java.util.function.Predicate`
```scala
scala> java.util.stream.Stream.of(1, 2, 3, 4).filter((_: Int) < 3).toArray
res3: Array[Object] = Array(1, 2)
```
  - convert a `scala.Function1` to a `java.util.function.Consumer`
```scala
scala> java.util.stream.Stream.of(1, 2, 3, 4).forEach(println(_: Int))
1
2
3
4
```
  - convert a `scala.Function2` to a `java.util.function.BinaryOperator`
```scala
scala> java.util.stream.Stream.of(1, 2, 3).reduce(0, (_: Int) + (_: Int))
res5: Int = 6
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
```scala
import sclib.io.fs._
```
functions to work with files and directories.

- all functions which can throw a exception are wrapped in a `Try`.
```scala
scala> for {
     |   wd <- dir("sclib-example")
     |   wd <- wd.createTemp              // create a temp work-dir (path is something like: '/tmp/sclib-example6964564891871111476')
     |   fh <- file(wd, "a-file")         // create a file under the work-dir
     |   _ <- fh.append("first line\n")   // write a line
     |   _ <- fh.append("second line")
     |   fs <- fh.size                    // file-size  (fh.size returns Try[Long])
     |   lc <- fh.lines.map(_.length)     // line-count (fh.lines returns Try[Iterator[String]])
     |   wc <- fh.slurp.map(_.size)       // word-count (fh.slurp returns Try[String])
     |   _ <- wd.deleteR                  // delete the work-dir recursive
     | } yield s"file size: ${fs}, line count: ${lc}, word count: ${wc}"
res0: scala.util.Try[String] = Success(file size: 22, line count: 2, word count: 22)
```

- type-class based `write`, `writeLines`, `append` and `appendLines` functions with instances for [basic types](http://j-keck.github.io/sclib/latest/api/#sclib.io.fs.Writable$)
```scala
scala> for {
     |   wd <- dir("sclib-example")
     |   wd <- wd.createTemp                                   // create a temp work-dir (path is something like: '/tmp/sclib-example6964564891871111476')
     |   fh <- file(wd, "a-file")                              // create a file under the work-dir
     |   _ <- fh.writeLines("1. apple")                        // string
     |   _ <- fh.appendLines(List("2. banana", "3. cherry"))   // list of string
     |   _ <- fh.append(4)                                     // int
     |   _ <- fh.append('.')                                   // char
     |   _ <- fh.append(Vector(' ', 'd', 'o', 'g'))            // vector of char
     |   content <- fh.slurp                                   // read the whole file 
     |   _ <- wd.deleteR                                       // delete the work-dir recursive
     | } yield content
res1: scala.util.Try[String] =
Success(1. apple
2. banana
3. cherry
4. dog)
```

#### net 
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.io.net.package)
```scala
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
```scala
```

- simple Union type
```scala
scala> import sclib.util.union._
import sclib.util.union._

scala> def f[A: (Int Or String)#Check](a: A): Int = a match {
     |   case i: Int => i
     |   case s: String => s.length
     | }
f: [A](a: A)(implicit evidence$1: <:<[sclib.util.union.Contra[sclib.util.union.Contra[A]],sclib.util.union.Contra[sclib.util.union.Contra[Int] with sclib.util.union.Contra[String]]])Int

scala> f(5)
res0: Int = 5

scala> f("hey")
res1: Int = 3

scala> // this throws a compiler error:
     | // f(5L) 
```



### "pattern's"
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.patterns.package)

```scala
import sclib.patterns._
```

#### AppF

simple *AppF*unction - expect's a function with receives a config and returns a Either

```scala
scala> import sclib.ops.either._
import sclib.ops.either._

scala> val action = for {
     |   a <- AppF{i: Int => i.right[String]}
     |   b <- AppF{i: Int => if(i < 5) (i * 10).right else "BOOM".left}
     |   c <- AppF.lift(33.right[String])
     | } yield (a, b, c)
action: sclib.z.EitherT[[β]sclib.z.Reader[Int,β],String,(Int, Int, Int)] = EitherT(Reader(<function1>))

scala> action.runEitherT.runReader(2)
res0: Either[String,(Int, Int, Int)] = Right((2,20,33))

scala> action.runEitherT.runReader(8)
res1: Either[String,(Int, Int, Int)] = Left(BOOM)
```

### (very) simple serialize / deserialize
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.serialization.simple.package)

i use this for little scala.js apps for the client / server communication
to keep the resulting '.js' files small.

values are converted to strings and prefixed with their length.
so the int value 31593 becomes "5:31593". to deserialize this value,
we give the deserializer the expected type, and it parse / converts the given string.


```scala
import sclib.serialization.simple._
```

#####for stdlib
  
```scala
scala> val s = Serialize("a simple string")
s: String = 15:a simple string

scala> Deserialize[String](s)
res0: Either[String,String] = Right(a simple string)

scala> val t = Serialize("a tuple with a string and a list" -> List(4, 23, 1))
t: String = 32:a tuple with a string and a list10:1:42:231:1

scala> Deserialize[(String, List[Int])](t)
res1: Either[String,(String, List[Int])] = Right((a tuple with a string and a list,List(4, 23, 1)))
```

#####for own types
  
  - define your types and the typeclass instances for serialization / deserialization
  
```scala
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
```scala
scala> val s = Serialize(C("the string", List(4, 2, 1), Right("i'm ok")))
s: String = 10:the string9:1:41:21:19:R6:i'm ok

scala> Deserialize[C](s)
res4: Either[String,C] = Right(C(the string,List(4, 2, 1),Right(i'm ok)))
```
