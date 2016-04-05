# sclib - simple scala utility library

  - stdlib extensions
  - utilities
  - zero runtime dependencies
  - jvm and scala.js bits

## quick start 

i publish the library to bintray for scala 2.10 and 2.11.
so you can add the following snippet to your `build.sbt` file.

- for the jvm:

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %% "sclib" % "0.2"

- for scala.js

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %%% "sclib" % "0.2"


## content

   - [stdlib extensions](#stdlib-extensions)
     - [Either](#either)
     - [List](#list)
     - [Try](#try)
     - [Java8 interoperability](#java8-interoperability)
   - [patterns](#patterns)
   - [(very) simple serialize / deserialize](#very-simple-serialize--deserialize)   


### stdlib extensions

#### Either
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
res5: scala.util.Either[Nothing,Int] = Right(5)
```

#### List
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

#### Try
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

### java8 interoperability
```scala
import sclib.ops.java8._
```

#####java.util.stream.Stream

  - convert to Iterator
```scala
scala> java.util.Arrays.asList(1, 2, 3, 4).stream.toIterator
res0: Iterator[Int] = non-empty iterator
```
  - convert to List
```scala
scala> java.util.Arrays.asList(1, 2, 3, 4).stream.toList
res1: List[Int] = List(1, 2, 3, 4)
```

#####create a 'java.util.function.Function' from a 'scala.Function1'
```scala
scala> java.util.Arrays.asList(1, 2, 3, 4).stream().map((_: Int) * 10).toArray
res2: Array[Object] = Array(10, 20, 30, 40)
```

#####create a 'java.util.function.Predicate' from a 'scala.Function1'
```scala
scala> java.util.Arrays.asList(1, 2, 3, 4).stream().filter((_:Int) < 3).toArray
res3: Array[Object] = Array(1, 2)
```


### "pattern's"
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
res0: String = a simple string

scala> val t = Serialize("a tuple with a string and a list" -> List(4, 23, 1))
t: String = 32:a tuple with a string and a list10:1:42:231:1

scala> Deserialize[(String, List[Int])](t)
res1: (String, List[Int]) = (a tuple with a string and a list,List(4, 23, 1))
```

#####for own types
  
  - define your type and the typeclass for serialization / deserialization
  
```scala
case class C(a: String, b: List[Int], c: Either[Int, String])

implicit val cSer = new Serialize[C]{
  override def apply(c: C): String = c match {
    case C(a, b, c) => Serialize(a) + Serialize(b) + Serialize(c)
  }
}

implicit val cDes = new Deserialize[C]{
  override def apply: sclib.ct.State[String, C] = for {
    a <- Deserialize[String]
    b <- Deserialize[List[Int]]
    c <- Deserialize[Either[Int, String]]
  } yield C(a, b, c)
}
```

  - use it
```scala
scala> val s = Serialize(C("the string", List(4, 2, 1), Right("i'm right")))
s: String = 10:the string9:1:41:21:112:R9:i'm right

scala> Deserialize[C](s)
res4: C = C(the string,List(4, 2, 1),Right(i'm right))
```
