# sclib - simple scala utility library

## zero dependencies

TODO


## stdlib extensions

### Either

```tut:silent:reset
import sclib.ops.either._
```

  - shorthand Left / Right constructor:
  
```tut
"a string".left[Int] 
4.right[String]
```

  - sequence on either
  
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


## (very) simple serialize / deserialize

values are converted to strings and prefixed with their length.
so the int value 31593 becomes "5:31593". to deserialize this value,
we give the deserializer the expected type, and it parse / converts the given string.


```tut:silent:reset
import sclib.serialization.simple._
```

  - for stdlib
  
```tut
val s = Serialize("a simple string")
Deserialize[String](s)

val t = Serialize("a tuple with a string and a list" -> List(4, 23, 1))
Deserialize[(String, List[Int])](t)
```

  - for own types
  
```tut
case class C(a: String, b: List[Int], c: Either[Int, String])

implicit val cSer = new Serialize[C]{
  override def apply(c: C): String = c match {
    case C(a, b, c) => Serialize(a) + Serialize(b) + Serialize(c)
  }
}

implicit val cDes = new Deserialize[C]{
  override def apply: sclib.State[String, C] = for {
    a <- Deserialize[String]
    b <- Deserialize[List[Int]]
    c <- Deserialize[Either[Int, String]]
  } yield C(a, b, c)
}

val s = Serialize(C("the string", List(4, 2, 1), Right("i'm right")))
Deserialize[C](s)
```
