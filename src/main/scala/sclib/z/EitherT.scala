package sclib.z

import sclib.ops.either._

/**
  * minimalistic `Either` monad transformer
  *
  * @example
  * {{{
  * scala> import sclib.z._
  * scala> import sclib.ops.either._
  *
  * scala> val et = EitherT[Function1[Int, ?], String, Int]{i => if(i < 10) i.right else "BOOM".left}
  * et: sclib.z.EitherT[[A]Int => A,String,Int] = EitherT(<function1>)
  *
  * scala> et.runEitherT(5)
  * res0: Either[String,Int] = Right(5)
  *
  * scala> et.runEitherT(50)
  * res1: Either[String,Int] = Left(BOOM)
  * }}}
  *
  */
case class EitherT[F[_], A, B](runEitherT: F[Either[A, B]]) {

  def map[C](f: B => C)(implicit F: Functor[F]): EitherT[F, A, C] = EitherT {
    F.map(runEitherT)(_.right.map(f))
  }

  def flatMap[AA >: A, D](f: B => EitherT[F, AA, D])(implicit F: Monad[F]): EitherT[F, AA, D] = EitherT {
    F.flatMap(runEitherT)(_.fold(l => F.pure(l.left[D]), r => f(r).runEitherT))
  }

  def flatMapF[C](f: B => F[Either[A, C]])(implicit F: Monad[F]): EitherT[F, A, C] = EitherT {
    F.flatMap(runEitherT)(_.fold(a => F.pure(a.left), f))
  }
}
