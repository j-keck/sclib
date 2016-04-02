package sclib.ct

import sclib.ops.either._

case class EitherT[F[_], A, B](runEitherT: F[Either[A, B]]) {

  def map[C](f: B => C)(implicit F: Functor[F]): EitherT[F, A, C] = EitherT {
    F.map(runEitherT)(_.right.map(f))
  }

  def flatMap[AA >: A, D](f: B => EitherT[F, AA, D])(implicit F: Monad[F]): EitherT[F, AA, D] = EitherT {
    F.flatMap(runEitherT) {
      case Left(v) => F.pure(v.left[D])
      case Right(r) => f(r).runEitherT
    }
  }
}
