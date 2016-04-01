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


## simple serialize / deserialize

TODO
