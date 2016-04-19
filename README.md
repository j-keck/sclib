# sclib - simple scala utility library

  - stdlib extensions
  - utilities
  - zero runtime dependencies
  - jvm and scala.js bits
  - [scaladoc](http://j-keck.github.io/sclib/latest/api/)

## quick start 

i publish the library to bintray for scala 2.10 and 2.11.
so you can add the following snippet to your `build.sbt` file.

- for the jvm:

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %% "sclib" % "0.3"

- for scala.js

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %%% "sclib" % "0.3"


## content

   - [stdlib extensions](#stdlib-extensions)
     - [Either](#either)
     - [Java8 interoperability](#java8-interoperability)
     - [List](#list)
     - [Option](#option)
     - [String](#string)
     - [Try](#try)
   - [io](#io)
   - [patterns](#patterns)
   - [(very) simple serialize / deserialize](#very-simple-serialize--deserialize)   


### stdlib extensions

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

  - sequence on either to reducing many `Either`s into a single `Either`
```scala
scala> EitherOps.sequence(List(3.right, 4.right))
res3: Either[Nothing,List[Int]] = Right(List(3, 4))

scala> EitherOps.sequence(List(3.right, 4.right, "BOOM".left))
res4: Either[String,List[Int]] = Left(BOOM)
```
   
  - right biased either
```scala
scala> for {
     |   a <- Right(1)
     |   b <- Right(4)
     | } yield a + b
res5: Either[Nothing,Int] = Right(5)
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
```

#### String
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.string)

```scala
import sclib.ops.string._
```

  - save / easy to compose toInt[T|E], toLong[T|E], toDouble[T|E], toChar[T|E] and toBoolean[T|E]
```scala
scala> "123".toIntT
res0: scala.util.Try[Int] = Success(123)

scala> "one".toIntT
res1: scala.util.Try[Int] = Failure(java.lang.NumberFormatException: For input string: "one")

scala> "123".toIntE
res2: Either[String,Int] = Right(123)

scala> "one".toIntE
res3: Either[String,Int] = Left('one' is not a Int)

scala> import sclib.ops.either._
import sclib.ops.either._

scala> for{
     |  a <- "123".toIntE
     |  b <- "44".toIntE
     | } yield a + b
res4: Either[String,Int] = Right(167)

scala> for{
     |  a <- "one".toIntE
     |  b <- "44".toIntE
     | } yield a + b
res5: Either[String,Int] = Left('one' is not a Int)
```

  - toDate[T|E]
```scala
implicit val sdf = new java.text.SimpleDateFormat("DD.MM.yyyy HH:mm:ss")
```

```scala
scala> "01.02.2020 16:30:10".toDateE
res6: Either[String,java.util.Date] = Right(Wed Jan 01 16:30:10 CET 2020)

scala> "Feb 01 16:30:10 2020".toDateE("MMM DD HH:mm:ss yyyy")
res7: Either[String,java.util.Date] = Right(Wed Jan 01 16:30:10 CET 2020)
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

  - sequence on `Try` to reducing many `Try`s into a single `Try`
```scala
scala> TryOps.sequence(3.success :: 44.success :: Nil)
res3: scala.util.Try[List[Int]] = Success(List(3, 44))

scala> TryOps.sequence(3.success :: 44.success :: "BOOM".failure :: Nil)
res4: scala.util.Try[List[Int]] = Failure(java.lang.Exception: BOOM)
```




### io
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.io.package)
```scala
import sclib.io._
```

- write a file
```scala
scala> val content = List("first line", "2. line", "third line", "4. line")
content: List[String] = List(first line, 2. line, third line, 4. line)

scala> file("/tmp/dummy").flatMap(_.writeLines(content))
res0: scala.util.Try[sclib.io.FSFile] = Success(FSFile(/tmp/dummy))
```

- read a file
```scala
scala> file("/tmp/dummy").flatMap(_.slurp)
res1: scala.util.Try[String] =
Success(first line
2. line
third line
4. line)
```

- all functions are wrapped in a `Try`, so errors are captured and it's easy to compose.
```scala
def info(p: String) = for {
  fh <- file(p)
  size <- fh.size
  mtime <- fh.mtime
  content <- fh.slurp
} yield s"name: $p, size: $size, mtime: $mtime, content: $content"
```
```scala
scala> info("/tmp/dummy")
res2: scala.util.Try[String] =
Success(name: /tmp/dummy, size: 38, mtime: 1461053226000, content: first line
2. line
third line
4. line)

scala> info("/not/existing/file")
res3: scala.util.Try[String] = Failure(java.nio.file.NoSuchFileException: /not/existing/file)
```

- type-class based `write`, `writeLines`, `append` and `appendLines` functions with instances for basic types. 
```scala
scala> for {
     |   fh <- file("/tmp/example")
     |   _ <- fh.writeLines("1. apple")                        // string
     |   _ <- fh.appendLines(List("2. banana", "3. cherry"))   // list of string
     |   _ <- fh.append(4)                                     // int
     |   _ <- fh.append('.')                                   // char
     |   _ <- fh.append(Vector(' ', 'd', 'o', 'g'))            // vector of char
     |   content <- fh.slurp
     |   _ <- fh.delete
     | } yield content
res4: scala.util.Try[String] =
Success(1. apple
2. banana
3. cherry
4. dog)
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
action: sclib.ct.EitherT[[B]sclib.ct.Reader[Int,B],String,(Int, Int, Int)] = EitherT(Reader(<function1>))

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
  override def apply(c: C): String = c match {
    case C(a, b, c) => Serialize(a) + Serialize(b) + Serialize(c)
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
