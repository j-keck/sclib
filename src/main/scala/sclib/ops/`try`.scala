package sclib.ops

import sclib.ops.either._

import scala.util.{Failure, Success, Try}

/**
  * `Try` extensions
  *
  * ''check the member documentation for examples''
  */
object `try` {

  /**
    * utility's for `Try`
    */
  object TryOps {
    /**
      * reducing many `Try`s into a single `Try`
      *
      * @example {{{
      * scala> import sclib.ops.`try`._
      * scala> TryOps.sequence(3.success :: 44.success :: Nil)
      * res0: scala.util.Try[List[Int]] = Success(List(3, 44))
      *
      * scala> TryOps.sequence(3.success :: 44.success :: "BOOM".failure :: Nil)
      * res1: scala.util.Try[List[Int]] = Failure(java.lang.Exception: BOOM)
      * }}}
      */
    def sequence[A](xs: List[Try[A]]): Try[List[A]] = xs match {
      case Nil => Success(Nil)
      case (Failure(t) :: _) => Failure(t)
      case (Success(x) :: ys) => sequence(ys).map(x :: _)
    }
  }

  /**
    * extensions on `Try` instances
    */
  implicit class TryOps[A](t: Try[A]) {

    /**
      * applies `ff` if this is a `Failure` or `sf` if this is a `Success`.
      *
      * @example {{{
      * scala> import sclib.ops.`try`._
      * scala> "<ERROR>".failure.fold(_.getMessage)(identity)
      * res0: String = <ERROR>
      * }}}
      */
    def fold[B](ff: Throwable => B)(sf: A => B): B = t match {
      case Success(a) => sf(a)
      case Failure(t) => ff(t)
    }

    /**
      * converts a 'Try[A]' to a 'Either[Throwable, A]'
      */
    def toEither: Either[Throwable, A] = fold(_.left[A])(_.right)
  }


  /**
    * shorthand constructor for `scala.util.Success`
    *
    * @example {{{
    * scala> import sclib.ops.`try`._
    * scala> 3.success
    * res0: scala.util.Try[Int] = Success(3)
    * }}}
    */
  implicit class Any2Success[A](a: A) {
    def success: Try[A] = Success(a)
  }


  /**
    * shorthand constructor for `Failure` from a `Throwable`
    *
    * @example {{{
    * scala> import sclib.ops.`try`._
    * scala> new IllegalArgumentException("BOOM").failure[Int]
    * res0: scala.util.Try[Int] = Failure(java.lang.IllegalArgumentException: BOOM)
    * }}}
    */
  implicit class Throwable2Failure(t: Throwable) {
    def failure[A]: Try[A] = Failure(t)
  }


  /**
    * shorthand constructor for `Failure` from a `String`
    *
    * @example {{{
    * scala> import sclib.ops.`try`._
    * scala> "BOOM".failure
    * res0: scala.util.Try[Nothing] = Failure(java.lang.Exception: BOOM)
    * }}}
    */
  implicit class String2Failure(s: String) {
    def failure[A]: Try[A] = Failure(new Exception(s))
  }

}
