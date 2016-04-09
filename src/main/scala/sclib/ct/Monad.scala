package sclib.ct

/**
  * minimalistic `Monad`
  */
trait Monad[F[_]] extends Functor[F]{

  /** lift the given value in the `Monad` */
  def pure[A](a: A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
