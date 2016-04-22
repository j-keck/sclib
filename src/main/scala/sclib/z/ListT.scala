package sclib.z

/**
  * minimalistic `List` monad transformer
  *
  * @example
  * {{{
  * scala> import sclib.z._
  * scala> import sclib.ops.`try`._
  * scala> (for {
  *      |   a <- ListT(List(1, 2).success)
  *      |   b <- ListT(List(3, 4).success)
  *      | } yield a + b).runListT
  * res0: scala.util.Try[List[Int]] = Success(List(4, 5, 5, 6))
  *
  * scala> (for {
  *      |   a <- ListT(List(1, 2).success)
  *      |   b <- ListT("BOOM".failure[List[Int]])
  *      | } yield a + b).runListT
  * res1: scala.util.Try[List[Int]] = Failure(java.lang.Exception: BOOM)
  * }}}
  */
case class ListT[F[_], A](runListT: F[List[A]]) {

  def map[B](f: A => B)(implicit F: Functor[F]): ListT[F, B] = ListT {
    F.map(runListT)(_.map(f))
  }

  def flatMap[B](f: A => ListT[F, B])(implicit F: Monad[F]): ListT[F, B] = ListT {
    F.flatMap(runListT)(_.map(f(_).runListT).fold(F.pure(List.empty[B])) {
      case (fbs1, fbs2) =>
        F.flatMap(fbs1)((bs1: List[B]) => F.map(fbs2)((bs2: List[B]) => bs1 ++ bs2))
    })
  }

  def flatMapF[B](f: A => F[List[B]])(implicit F: Monad[F]): ListT[F, B] = ListT {
    F.flatMap(runListT)(_.map(f).fold(F.pure(List.empty[B])) {
      case (fbs1, fbs2) =>
        F.flatMap(fbs1)((bs1: List[B]) => F.map(fbs2)((bs2: List[B]) => bs1 ++ bs2))
    })
  }

  /**
    * head of the list
    *
    * @example
    * {{{
    * scala> import sclib.z._
    * scala> import sclib.ops.`try`._
    * scala> ListT(List(1, 2, 3).success).head
    * res0: scala.util.Try[Int] = Success(1)
    * scala> ListT("BOOM".failure[List[Int]]).head
    * res1: scala.util.Try[Int] = Failure(java.lang.Exception: BOOM)
    * scala> ListT(List.empty.success).head
    * res2: scala.util.Try[Nothing] = Failure(java.util.NoSuchElementException: head of empty list)
    * }}}
    */
  def head(implicit F: Functor[F]): F[A] = F.map(runListT)(_.head)

  /**
    * tail of the list
    *
    * @example
    * {{{
    * scala> import sclib.z._
    * scala> import sclib.ops.`try`._
    * scala> ListT(List(1, 2, 3).success).tail
    * res0: sclib.z.ListT[scala.util.Try,Int] = ListT(Success(List(2, 3)))
    * scala> ListT("BOOM".failure[List[Int]]).tail
    * res1: sclib.z.ListT[scala.util.Try,Int] = ListT(Failure(java.lang.Exception: BOOM))
    * scala> ListT(List.empty.success).tail
    * res2: sclib.z.ListT[scala.util.Try,Nothing] = ListT(Failure(java.lang.UnsupportedOperationException: tail of empty list))
    * }}}
    */
  def tail(implicit F: Functor[F]): ListT[F, A] = ListT(F.map(runListT)(_.tail))

  /**
    * prepend the given element to the list
    * @example
    * {{{
    * scala> import sclib.z._
    * scala> import sclib.ops.`try`._
    * scala> 1.success :: ListT(List(2, 3).success)
    * res0: sclib.z.ListT[scala.util.Try,Int] = ListT(Success(List(1, 2, 3)))
    * scala> "BOOM".failure :: ListT(List(2, 3).success)
    * res1: sclib.z.ListT[scala.util.Try,Int] = ListT(Failure(java.lang.Exception: BOOM))
    * }}}
    */
  def ::(a: F[A])(implicit F: Monad[F]): ListT[F, A] = ListT {
    F.flatMap(runListT) { x: List[A] =>
      F.map(a)(_ :: x)
    }
  }

  /**
    * concat
    *
    * @example
    * {{{
    * scala> import sclib.z._
    * scala> import sclib.ops.`try`._
    * scala> ListT(List(1, 2).success) ++ List(3, 4).success
    * res0: sclib.z.ListT[scala.util.Try,Int] = ListT(Success(List(1, 2, 3, 4)))
    * scala> ListT(List(1, 2).success) ++ "BOOM".failure
    * res1: sclib.z.ListT[scala.util.Try,Int] = ListT(Failure(java.lang.Exception: BOOM))
    * }}}*/
  def ++(xs: F[List[A]])(implicit F: Monad[F]): ListT[F, A] = ListT {
    F.flatMap(runListT) { x: List[A] =>
      F.map(xs)(x ++ _)
    }
  }

  /**
    * concat
    *
    * @example
    * {{{
    * scala> import sclib.z._
    * scala> import sclib.ops.`try`._
    * scala> ListT(List(1, 2).success) ++ ListT(List(3, 4).success)
    * res0: sclib.z.ListT[scala.util.Try,Int] = ListT(Success(List(1, 2, 3, 4)))
    * }}}
    */
  def ++(other: ListT[F, A])(implicit F: Monad[F]): ListT[F, A] = ListT {
    F.flatMap(runListT) { x: List[A] =>
      F.flatMap(other.runListT) { (y: List[A]) =>
        F.pure(x ++ y)
      }
    }
  }
}

object ListT {
  def apply[F[_], A](xs: List[A])(implicit F: Monad[F]): ListT[F, A] = ListT(F.pure(xs))
}
