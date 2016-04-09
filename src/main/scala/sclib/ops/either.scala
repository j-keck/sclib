package sclib.ops

/**
  * `Either` extensions
  *
  * ''check the member documentation for examples''
  */
object either {

  /**
    * utility's for `Either`
    */
  object EitherOps {
    /**
      * reducing many `Either`s into a single `Either`
      *
      * @example {{{
      * scala> import sclib.ops.either._
      *
      * scala> EitherOps.sequence(List(3.right, 4.right))
      * res0: Either[Nothing,List[Int]] = Right(List(3, 4))
      *
      * scala> EitherOps.sequence(List(3.right, 4.right, "BOOM".left))
      * res1: Either[String,List[Int]] = Left(BOOM)
      * }}}
      */
    def sequence[A, B](xs: List[Either[A, B]]): Either[A, List[B]] = xs match {
      case Nil => Right(Nil)
      case (Left(x) :: _) => Left(x)
      case (Right(x) :: ys) => sequence(ys).map(x :: _)
    }
  }

  /**
    * extensions on `Either` instances
    */
  implicit class EitherOps[A, B](e: Either[A, B]) {
    /** apply the given function if it's a `Right` */
    def map[C](f: B => C): Either[A, C] = e.right.map(f)

    /** apply the given function if it's a `Right` */
    def flatMap[C](f: B => Either[A, C]) = e.right.flatMap(f)

    /** get the current value if it's a `Right` otherwise return the given argument */
    def getOrElse[BB >: B](or: => BB): BB = e.right.getOrElse(or)

    /** get the current value as a `Some` if it's a `Right` otherwise return `None` */
    def toOption: Option[B] = e.right.toOption
  }

  /**
    * shorthand constructor for `Left`
    *
    * @example {{{
    * scala> import sclib.ops.either._
    * scala> "BOOM".left
    * res0: Either[String,Nothing] = Left(BOOM)
    * }}}
    */
  implicit class Any2Left[A](a: A){
    def left[B]: Either[A, B] = Left[A, B](a)
  }

  /**
    * shorthand constructor for `Right`
    *
    * @example {{{
    * scala> import sclib.ops.either._
    * scala> 1.right
    * res0: Either[Nothing,Int] = Right(1)
    * }}}
    */
  implicit class Any2Right[B](b: B){
    def right[A]: Either[A, B] = Right[A, B](b)
  }

}
