package sclib.ops

/**
  * scala.Either extensions
  */
object either {

  object EitherOps {
    def sequence[A, B](xs: List[Either[A, B]]): Either[A, List[B]] = xs match {
      case Nil => Right(Nil)
      case (Left(x) :: _) => Left(x)
      case (Right(x) :: ys) => sequence(ys).map(x :: _)
    }
  }

  implicit class EitherOps[A, B](e: Either[A, B]) {
    def map[C](f: B => C): Either[A, C] = e.right.map(f)
    def flatMap[C](f: B => Either[A, C]) = e.right.flatMap(f)
  }

  implicit class Any2Left[A](a: A){
    def left[B]: Either[A, B] = Left[A, B](a)
  }

  implicit class Any2Right[B](b: B){
    def right[A]: Either[A, B] = Right[A, B](b)
  }

}
