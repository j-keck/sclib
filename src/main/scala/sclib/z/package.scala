package sclib

import scala.util.{Success, Try}

/**
  * functor, monad, monad-transformer
  *
  * - for zero runtime dependencies of this library, some concepts are reimplemented here
  * - very basic / minimal `Functor` and `Monad` implementations
  *
  * ''check the member documentation for examples''
  */
package object z {


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

  /** `Vector`s `Functor` and `Monad` instances */
  implicit def vectorInstances = new Functor[Vector] with Monad[Vector]{
    override def map[A, B](fa: Vector[A])(f: (A) => B): Vector[B] = fa.map(f)

    override def flatMap[A, B](fa: Vector[A])(f: (A) => Vector[B]): Vector[B] = fa.flatMap(f)

    override def pure[A](a: A): Vector[A] = Vector(a)
  }

  
  /** `Set`s `Functor` and `Monad` instances */
  implicit def setInstances = new Functor[Set] with Monad[Set]{
    override def map[A, B](fa: Set[A])(f: (A) => B): Set[B] = fa.map(f)

    override def flatMap[A, B](fa: Set[A])(f: (A) => Set[B]): Set[B] = fa.flatMap(f)

    override def pure[A](a: A): Set[A] = Set(a)
  }

  /** `Iterator`s `Functor` and `Monad` instances */
  implicit def iteratorInstances = new Functor[Iterator] with Monad[Iterator]{
    override def map[A, B](fa: Iterator[A])(f: (A) => B): Iterator[B] = fa.map(f)

    override def flatMap[A, B](fa: Iterator[A])(f: (A) => Iterator[B]): Iterator[B] = fa.flatMap(f)

    override def pure[A](a: A): Iterator[A] = Iterator.single(a)
  }

  /** `Function0`s `Functor` and `Monad` instances */
  implicit def function0Instances = new Functor[Function0] with Monad[Function0] {
    override def map[A, B](fa: () => A)(f: (A) => B): () => B = () => f(fa())

    override def flatMap[A, B](fa: () => A)(f: (A) => () => B): () => B = f(fa())

    override def pure[A](a: A): () => A = () => a
  }


  /** `Function1`s `Functor` and `Monad` instances */
  implicit def function1Instances[A] = new Functor[Function1[A, ?]] with Monad[Function1[A, ?]] {
    override def map[B, C](fa: (A) => B)(f: (B) => C): (A) => C = a => f(fa(a))

    override def flatMap[B, C](fa: (A) => B)(f: (B) => (A) => C): (A) => C = a => f(fa(a))(a)

    override def pure[B](b: B): (A) => B = _ => b
  }


  /** `Either`s `Functor` and `Monad` instances */
  implicit def eitherInstance[A] = new Functor[Either[A, ?]] with Monad[Either[A, ?]] {
    override def map[B, C](fa: Either[A, B])(f: (B) => C): Either[A, C] = fa.right.map(f)

    override def flatMap[B, C](fa: Either[A, B])(f: (B) => Either[A, C]): Either[A, C] = fa.right.flatMap(f)

    override def pure[B](b: B): Either[A, B] = Right(b)
  }


  /** `Try`s `Functor` and `Monad` instances */
  implicit def tryInstance = new Functor[Try] with Monad[Try] {
    override def map[A, B](fa: Try[A])(f: (A) => B): Try[B] = fa.map(f)

    override def flatMap[A, B](fa: Try[A])(f: (A) => Try[B]): Try[B] = fa.flatMap(f)

    /** lift the given value in the `Monad` */
    override def pure[A](a: A): Try[A] = Success(a)
  }


  /** `Reader`s `Functor` and `Monad` instances */
  implicit def readerInstances[C] = new Functor[Reader[C, ?]] with Monad[Reader[C, ?]] {
    override def pure[A](a: A): Reader[C, A] = Reader(_ => a)

    override def flatMap[A, B](fa: Reader[C, A])(f: (A) => Reader[C, B]): Reader[C, B] = fa.flatMap(f)

    override def map[A, B](fa: Reader[C, A])(f: (A) => B): Reader[C, B] = fa.map(f)
  }


  /** `State`s `Functor` and `Monad` instances */
  implicit def stateInstances[S] = new Functor[State[S, ?]] with Monad[State[S, ?]] {
    override def pure[A](a: A): State[S, A] = State(s => (a, s))

    override def flatMap[A, B](fa: State[S, A])(f: (A) => State[S, B]): State[S, B] = fa.flatMap(f)

    override def map[A, B](fa: State[S, A])(f: (A) => B): State[S, B] = fa.map(f)
  }
}
