package sclib.z

/**
  * minimalistic `State` monad transformer
  *
  * @example
  * {{{
  *  scala> import sclib.z._
  *  scala> import sclib.ops.`try`._
  *  scala> import scala.util.Try
  *
  *  scala> val pf = StateT{i: Int => if(i < 10) (i, i + 1).success else "BOOM".failure}
  *  scala> val action = for {
  *       |   a <- pf
  *       |   b <- pf
  *       | } yield a -> b
  *
  *  scala> action.run(0)
  *  res1: scala.util.Try[((Int, Int), Int)] = Success(((0,1),2))
  *
  *  scala> action.run(10)
  *  res2: scala.util.Try[((Int, Int), Int)] = Failure(java.lang.Exception: BOOM)
  * }}}
  */
case class StateT[F[_], S, A](runStateT: F[S => F[(A, S)]]) {

  def map[B](f: A => B)(implicit F: Monad[F]): StateT[F, S, B] = StateT { s =>
    F.map(run(s)) { case (a, s) => (f(a), s) }
  }

  def flatMap[B](f: A => StateT[F, S, B])(implicit F: Monad[F]): StateT[F, S, B] = StateT { s =>
    F.flatMap(run(s)) {
      case (a, s) =>
        f(a).run(s)
    }
  }

  def flatMapF[B](f: A => F[B])(implicit F: Monad[F]): StateT[F, S, B] = StateT { s =>
    F.flatMap(runStateT) { fa =>
      F.flatMap(fa(s)) {
        case (a, s) => F.map(f(a))((_, s))
      }
    }
  }

  def run(s: S)(implicit F: Monad[F]): F[(A, S)] = {
    F.flatMap(runStateT)(f => f(s))
  }

  def eval(s: S)(implicit F: Monad[F]): F[A] = {
    F.map(run(s))(_._1)
  }
}

object StateT {
  def apply[F[_], S, A](f: S => F[(A, S)])(implicit F: Monad[F]): StateT[F, S, A] = new StateT(F.pure(f))
}
