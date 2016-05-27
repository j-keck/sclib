package sclib.z

/**
  * minimalistic `Monad`
  */
trait Monad[F[_]] extends Functor[F] {

  /** lift the given value in the `Monad` */
  def pure[A](a: A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  override def map[A, B](fa: F[A])(f: (A) => B): F[B] =
    flatMap(fa) { a =>
      pure(f(a))
    }
}
