package sclib.ops

import scala.util.{Either, Try}
import sclib.ops.`try`._

import scala.collection.{SeqLike, TraversableLike}
import scala.collection.generic.CanBuildFrom

object either extends either

/**
  * `Either` extensions
  *
  * ''check the member documentation for examples''
  */
trait either {

  /**
    * utility's for `Either`
    */
  object EitherOps {

    /**
      * reducing many `Either`s into a single `Either`
      *
      * @example {{{
      * scala> import sclib.ops.either._
      *
      * scala> EitherOps.sequence(List(3.right, 4.right))
      * res0: Either[Nothing,List[Int]] = Right(List(3, 4))
      *
      * scala> EitherOps.sequence(List(3.right, 4.right, "BOOM".left))
      * res1: Either[String,List[Int]] = Left(BOOM)
      *
      * scala> EitherOps.sequence(Vector(2.right, 5.right))
      * res2: scala.util.Either[Nothing,scala.collection.immutable.Vector[Int]] = Right(Vector(2, 5))
      * }}}
      */
    def sequence[A, B, CC[X] <: Traversable[X]](value: CC[Either[A, B]])(
        implicit cbf: CanBuildFrom[Nothing, B, CC[B]]): Either[A, CC[B]] = {

      val b = {
        val builder = collection.breakOut[CC[Either[A, B]], B, CC[B]]
        builder(value)
      }
      b.sizeHint(value)

      def go(xs: Traversable[Either[A, B]]): Either[A, CC[B]] = xs.headOption match {
        case Some(Left(x)) => x.left
        case Some(Right(x)) =>
          b += x
          go(xs.tail)
        case None => b.result.right
      }

      go(value)
    }
  }

  /**
    * extensions on `Either` instances
    */
  implicit class EitherOps[A, B](e: Either[A, B]) {

    /** apply the given function if it's a `Right` */
    def map[BB](f: B => BB): Either[A, BB] = e.right.map(f)

    /** apply the given function if it's a `Right` */
    def flatMap[BB](f: B => Either[A, BB]): Either[A, BB] = e.right.flatMap(f)

    /** get the current value if it's a `Right` otherwise return the given argument */
    def getOrElse[BB >: B](or: => BB): BB = e.right.getOrElse(or)

    /** get the current value as a `Some` if it's a `Right` otherwise return `None` */
    def toOption: Option[B] = e.right.toOption

    /** get the current value as a `Success` if it's a `Right` otherwise return `Failure`
      * with the Left site
      */
    def toTry: Try[B] = e.fold(_.toString.failure, _.success)

    /** map both */
    def bimap[AA, BB](fl: A => AA, fr: B => BB) = e.fold(fl, fr)

    /** map over left */
    def leftMap[AA](f: A => AA): Either[AA, B] = e.left.map(f)

    /** flatMap over left */
    def leftFlatMap[AA](f: A => Either[AA, B]): Either[AA, B] = e.left.flatMap(f)
  }

  /**
    * shorthand constructor for `Left`
    *
    * @example {{{
    * scala> import sclib.ops.either._
    * scala> "BOOM".left
    * res0: Either[String,Nothing] = Left(BOOM)
    * }}}
    */
  implicit class Any2Left[A](a: A) {
    def left[B]: Either[A, B] = Left[A, B](a)
  }

  /**
    * shorthand constructor for `Right`
    *
    * @example {{{
    * scala> import sclib.ops.either._
    * scala> 1.right
    * res0: Either[Nothing,Int] = Right(1)
    * }}}
    */
  implicit class Any2Right[B](b: B) {
    def right[A]: Either[A, B] = Right[A, B](b)
  }
}
