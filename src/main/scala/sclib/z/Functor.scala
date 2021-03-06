package sclib.z

/**
  * minimalistic `Functor`
  */
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
