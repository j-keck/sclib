package sclib.z

import scala.util.{Failure, Success, Try}
import sclib.ops.`try`._

/**
  * minimalistic `Try` monad transformer
  *
  * @example
  * {{{
  * scala> import sclib.z._
  * scala> import sclib.ops.`try`._
  * scala> val tt = TryT[({type L[A] = Function1[Int, A]})#L, Int]{i => if(i < 10) i.success else "BOOM".failure}
  *
  * scala> tt.map(_ * 10).runTryT(5)
  * res0: scala.util.Try[Int] = Success(50)
  *
  * scala> tt.map(_ * 10).runTryT(10)
  * res1: scala.util.Try[Int] = Failure(java.lang.Exception: BOOM)
  * }}}
  *
  */
case class TryT[F[_], A](runTryT: F[Try[A]]) {

  def map[B](f: A => B)(implicit F: Functor[F]): TryT[F, B] = TryT {
    F.map(runTryT)(_.map(f))
  }

  def flatMap[B](f: A => TryT[F, B])(implicit F: Monad[F]): TryT[F, B] = TryT {
    F.flatMap(runTryT)(_.fold(t => F.pure(t.failure[B]))(f(_).runTryT))
  }

  def flatMapF[B](f: A => F[Try[B]])(implicit F: Monad[F]): TryT[F, B] = TryT {
    F.flatMap(runTryT)(_.fold(t => F.pure(t.failure[B]))(x => f(x)))
  }
}
