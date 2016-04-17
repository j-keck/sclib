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
     - [List](#list)
     - [Try](#try)
     - [Java8 interoperability](#java8-interoperability)
   - [io](#io)
   - [patterns](#patterns)
   - [(very) simple serialize / deserialize](#very-simple-serialize--deserialize)   


### stdlib extensions

#### Either
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.either$)

```tut:silent:reset
import sclib.ops.either._
```

  - shorthand Left / Right constructor:
```tut
"a string".left
"a string".left[Int] 
4.right[String]
```

  - sequence on either to reducing many `Either`s into a single `Either`
```tut
EitherOps.sequence(List(3.right, 4.right))

EitherOps.sequence(List(3.right, 4.right, "BOOM".left))
```
   
  - right biased either
```tut
for {
  a <- Right(1)
  b <- Right(4)
} yield a + b
```

#### List
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.list$)

```tut:silent:reset
import sclib.ops.list._
```

  - unfoldLeft / unfoldRight
```tut
ListOps.unfoldRight(0){ i =>
  if(i > 10) None else Some((i, i + 1))
}
```

#### Try
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.try$)

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

  - sequence on `Try` to reducing many `Try`s into a single `Try`
```tut
TryOps.sequence(3.success :: 44.success :: Nil)
TryOps.sequence(3.success :: 44.success :: "BOOM".failure :: Nil)
```

### java8 interoperability
[scaladoc](http://j-keck.github.io/sclib/latest/api/#sclib.ops.java8$)

```tut:silent:reset
import sclib.ops.java8._
```

#####java.util.stream.Stream

  - convert to 'scala.collection.Iterator'
```tut
java.util.stream.Stream.of(1, 2, 3, 4).toIterator
```
  - convert to 'scala.collection.immutable.List'
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
```tut:silent:reset
import sclib.io._
```

- write a file
```tut
val content = List("first line", "2. line", "third line", "4. line")
file("/tmp/dummy").flatMap(_.writeLines(content))
```

- read a file
```tut
file("/tmp/dummy").flatMap(_.slurp)
```

- all functions are wrapped in a `Try`, so errors are captured and it's easy to compose.
```tut:silent
def info(p: String) = for {
  fh <- file(p)
  size <- fh.size
  mtime <- fh.mtime
  content <- fh.slurp
} yield s"name: $p, size: $size, mtime: $mtime, content: $content"
```
```tut
info("/tmp/dummy")
info("/not/existing/file")
```

- type-class based `write`, `writeLines`, `append` and `appendLines` functions with instances for basic types. 
```tut
for {
  fh <- file("/tmp/example")
  _ <- fh.writeLines("1. apple")                        // string
  _ <- fh.appendLines(List("2. banana", "3. cherry"))   // list of string
  _ <- fh.append(4)                                     // int
  _ <- fh.append('.')                                   // char
  _ <- fh.append(Vector(' ', 'd', 'o', 'g'))            // vector of char
  content <- fh.slurp
  _ <- fh.delete
} yield content
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
```tut
val s = Serialize(C("the string", List(4, 2, 1), Right("i'm ok")))
Deserialize[C](s)
```
