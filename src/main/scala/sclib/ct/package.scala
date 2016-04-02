package sclib

/**
  * functor, monad, monad-transformer
  */
package object ct {

  implicit def readerInstances[C] = new Functor[({type L[A] = Reader[C, A]})#L] with Monad[({type L[A] = Reader[C, A]})#L] {

    override def pure[A](a: A): Reader[C, A] = Reader(_ => a)

    override def flatMap[A, B](fa: Reader[C, A])(f: (A) => Reader[C, B]): Reader[C, B] = fa.flatMap(f)

    override def map[A, B](fa: Reader[C, A])(f: (A) => B): Reader[C, B] = fa.map(f)
  }


  implicit def stateInstances[S] = new Functor[({type L[A] = State[S, A]})#L] with Monad[({type L[A] = State[S, A]})#L] {

    override def pure[A](a: A): State[S, A] = State(s => (a, s))

    override def flatMap[A, B](fa: State[S, A])(f: (A) => State[S, B]): State[S, B] = fa.flatMap(f)

    override def map[A, B](fa: State[S, A])(f: (A) => B): State[S, B] = fa.map(f)
  }
}
