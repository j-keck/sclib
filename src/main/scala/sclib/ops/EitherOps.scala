package sclib.ops

/**
  * scala.Either extensions
  */
object EitherOps {
  def sequence[A, B](xs: List[Either[A, B]]): Either[A, List[B]] = xs match {
    case Nil => Right(Nil)
    case (Left(x) :: _) => Left(x)
    case (Right(x) :: ys) => sequence(ys).right.map(x :: _)
  }
}
