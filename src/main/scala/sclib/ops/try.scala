package sclib.ops

import sclib.ops.either._

import scala.collection.generic.CanBuildFrom
import scala.util.{Failure, Success, Try}

object `try` extends `try`

/**
  * `Try` extensions
  *
  * ''check the member documentation for examples''
  */
trait `try` {

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

    /**
      * zip two Try's
      *
      * returns a `Failure` if any of the given `Try`s are a `Failure`
      *
      * @example
      * {{{
      * scala> import sclib.ops.`try`._
      * scala> import scala.util.Try
      * scala> val z = 5.success.zip(11.success)
      * res0: Try[(Int, Int)] = Success(5, 11)
      * scala> z.map(Function.tupled(_ + _))
      * res1: Try[Int] = Success(16)
      * }}}
      */
    def zip[B](other: Try[B]): Try[(A, B)] = (t, other) match {
      case (Success(a), Success(b)) => Success(a -> b)
      case (Failure(t), _)          => t.failure
      case (_, Failure(t))          => t.failure
    }

    /**
      * apply the given function if it's a failure
      *
      * (read as '''map o'''ther)
      *
      * @example
      * {{{
      * scala> import sclib.ops.`try`._
      * scala> import scala.util.Try
      * scala> Try(1/0)
      * res0: Try[Int] = Failure(java.lang.ArithmeticException: / by zero)
      * scala> Try(1/0).mapO(_ => new java.lang.Exception("unable to calculate: 1/0"))
      * res1: Try[Int] = Failure(java.lang.Exception: unable to calculate: 1/0)
      * }}}
      */
    def mapO(f: Throwable => Throwable): Try[A] = t.transform(_.success, f(_).failure)

    /**
      * apply the given function if it's a failure
      *
      * (read as '''flatMap o'''ther)
      *
      * @example
      * {{{
      * scala> import sclib.ops.`try`._
      * scala> import scala.util.Try
      * scala> Try(1/0)
      * res0: Try[Int] = Failure(java.lang.ArithmeticException: / by zero)
      * scala> Try(1/0).flatMapO(_ => "unable to calculate: 1/0".failure)
      * res1: Try[Int] = Failure(java.lang.Exception: unable to calculate: 1/0)
      * scala> Try(1/0).flatMapO(_ => 0.success)
      * res2: Try[Int] = Success(0)
      * }}}
      */
    def flatMapO(f: Throwable => Try[A]): Try[A] = t.transform(_.success, f)
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

  /**
    * reducing many `Try`s into a single `Try`
    */
  implicit class TraversableOfTry[A, CC[X] <: Traversable[X]](ts: CC[Try[A]]) {

    /**
      * reducing many `Try`s into a single `Try`
      *
      * @example {{{
      * scala> import sclib.ops.`try`._
      * scala> List(3.success, 44.success).sequence
      * res0: scala.util.Try[List[Int]] = Success(List(3, 44))
      *
      * scala> List(3.success, "BOOM".failure, 44.success).sequence
      * res1: scala.util.Try[List[Int]] = Failure(java.lang.Exception: BOOM)
      *
      * scala> Vector(1.success, 2.success).sequence
      * res2: scala.util.Try[scala.collection.immutable.Vector[Int]] = Success(Vector(1, 2))
      * }}}
      */
    def sequence(implicit cbf: CanBuildFrom[Nothing, A, CC[A]]): Try[CC[A]] = {

      val b = {
        val builder = collection.breakOut[CC[Try[A]], A, CC[A]]
        builder(ts)
      }
      b.sizeHint(ts)

      def go(xs: Traversable[Try[A]]): Try[CC[A]] = xs.headOption match {
        case Some(Failure(x)) => x.failure
        case Some(Success(x)) =>
          b += x
          go(xs.tail)
        case None => b.result.success
      }

      go(ts)
    }
  }
}
