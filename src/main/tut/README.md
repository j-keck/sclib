# sclib - simple scala utility library

## 

i publish the library to bintray for scala 2.10 and 2.11

- for the jvm:

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %% "sclib" % "0.1"

- for scala.js

        resolvers += Resolver.bintrayRepo("j-keck", "maven")
        libraryDependencies += "net.jkeck" %%% "sclib" % "0.1"


## zero runtime dependencies

TODO


## stdlib extensions

### Either

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

### List

```tut:silent:reset
import sclib.ops.list._
```

  - unfoldLeft / unfoldRight
    
```tut
ListOps.unfoldRight(0){ i =>
  if(i > 10) None else Some((i, i + 1))
}
```

### Try
```tut:silent:reset
import sclib.ops.`try`._
```

  - shorthand constructor for `scala.util.Success`
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

## java8
```tut:silent:reset
import sclib.ops.java8._
```

#####java.util.stream.Stream

  - convert to Iterator
```tut
java.util.Arrays.asList(1, 2, 3, 4).stream.toIterator
```
  - convert to List
```tut
java.util.Arrays.asList(1, 2, 3, 4).stream.toList
```

#####create a 'java.util.function.Function' from a 'scala.Function1'
```tut
java.util.Arrays.asList(1, 2, 3, 4).stream().map((_: Int) * 10).toArray
```

#####create a 'java.util.function.Predicate' from a 'scala.Function1'
```tut
java.util.Arrays.asList(1, 2, 3, 4).stream().filter((_:Int) < 3).toArray
```


## "design pattern's"

```tut:silent:reset
import sclib.dp._
```

### AppF

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

## (very) simple serialize / deserialize

values are converted to strings and prefixed with their length.
so the int value 31593 becomes "5:31593". to deserialize this value,
we give the deserializer the expected type, and it parse / converts the given string.


```tut:silent:reset
import sclib.serialization.simple._
```

####for stdlib
  
```tut
val s = Serialize("a simple string")
Deserialize[String](s)

val t = Serialize("a tuple with a string and a list" -> List(4, 23, 1))
Deserialize[(String, List[Int])](t)
```

####for own types
  
  - define your type and the typeclass for serialization / deserialization
  
```tut:silent
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
```tut
val s = Serialize(C("the string", List(4, 2, 1), Right("i'm right")))
Deserialize[C](s)
```
