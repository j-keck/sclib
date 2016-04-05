package sclib.ct

import sclib.ops.either._

/**
  * minimalistic `Either` monad transformer
  *
  * @example {{{
  * scala> import sclib.ct._
  * scala> scala> import sclib.ops.either._
  *
  * scala> scala> val et = EitherT[Function0, Int, Int]{() => 10.right}
  * scala> et: sclib.ct.EitherT[Function0,Int,Int] = EitherT(<function0>)
  *
  * scala> scala> et.map(_ * 10).map(_ * 10).runEitherT.apply()
  * scala> res0: Either[Int,Int] = Right(1000)
  * }}}
  *
  */
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
