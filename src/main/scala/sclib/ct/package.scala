package sclib

/**
  * == functor, monad, monad-transformer
  *
  *   - for zero runtime dependencies of this library, some concepts are reimplemented here
  *   - very basic / minimal `Functor` and `Monad` implementations
  *
  * ''check the member documentation for examples''
  */
package object ct {


  /** `Option`s `Functor` and `Monad` instances */
  implicit def optionInstances = new Functor[Option] with Monad[Option] {
    override def map[A, B](fa: Option[A])(f: (A) => B): Option[B] = fa.map(f)

    override def flatMap[A, B](fa: Option[A])(f: (A) => Option[B]): Option[B] = fa.flatMap(f)

    override def pure[A](a: A): Option[A] = Some(a)
  }


  /** `List`s `Functor` and `Monad` instances */
  implicit def listInstances = new Functor[List] with Monad[List] {
    override def map[A, B](fa: List[A])(f: (A) => B): List[B] = fa.map(f)

    override def flatMap[A, B](fa: List[A])(f: (A) => List[B]): List[B] = fa.flatMap(f)

    override def pure[A](a: A): List[A] = List(a)
  }


  /** `Function0`s `Functor` and `Monad` instances */
  implicit def function0Instances = new Functor[Function0] with Monad[Function0] {
    override def map[A, B](fa: () => A)(f: (A) => B): () => B = () => f(fa())

    override def flatMap[A, B](fa: () => A)(f: (A) => () => B): () => B = f(fa())

    override def pure[A](a: A): () => A = () => a
  }


  /** `Either`s `Functor` and `Monad` instances */
  implicit def eitherInstance[A] = new Functor[({type L[B] = Either[A, B]})#L] with Monad[({type L[B] = Either[A, B]})#L] {
    override def map[B, C](fa: Either[A, B])(f: (B) => C): Either[A, C] = fa.right.map(f)

    override def flatMap[B, C](fa: Either[A, B])(f: (B) => Either[A, C]): Either[A, C] = fa.right.flatMap(f)

    override def pure[B](b: B): Either[A, B] = Right(b)
  }


  /** `Reader`s `Functor` and `Monad` instances */
  implicit def readerInstances[C] = new Functor[({type L[A] = Reader[C, A]})#L] with Monad[({type L[A] = Reader[C, A]})#L] {
    override def pure[A](a: A): Reader[C, A] = Reader(_ => a)

    override def flatMap[A, B](fa: Reader[C, A])(f: (A) => Reader[C, B]): Reader[C, B] = fa.flatMap(f)

    override def map[A, B](fa: Reader[C, A])(f: (A) => B): Reader[C, B] = fa.map(f)
  }


  /** `State`s `Functor` and `Monad` instances */
  implicit def stateInstances[S] = new Functor[({type L[A] = State[S, A]})#L] with Monad[({type L[A] = State[S, A]})#L] {
    override def pure[A](a: A): State[S, A] = State(s => (a, s))

    override def flatMap[A, B](fa: State[S, A])(f: (A) => State[S, B]): State[S, B] = fa.flatMap(f)

    override def map[A, B](fa: State[S, A])(f: (A) => B): State[S, B] = fa.map(f)
  }
}
