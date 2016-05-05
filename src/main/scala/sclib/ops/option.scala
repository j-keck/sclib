package sclib.ops

import scala.collection.generic.CanBuildFrom

object option extends option

/**
  * `Option` extensions
  *
  * ''check the member documentation for examples''
  */
trait option {

  /**
    * shorthand constructor for `None`
    **/
  def none[A]: Option[A] = None


  /**
    * shorthand constructor for `Some`
    */
  implicit class Any2Some[A](a: A) {
    def some: Option[A] = Some(a)
  }


  /**
    * reducing many `Option`s into a single `Option`
    */
  implicit class TraversableOfOption[A, CC[X] <: Traversable[X]](ts: CC[Option[A]]) {
    /**
      * reducing many `Option`s into a single `Option`
      *
      * @example {{{
      * scala> import sclib.ops.option._
      * scala> List(3.some, 44.some).sequence
      * res0: Option[List[Int]] = Some(List(3, 44))
      *
      * scala> List(3.some, none, 44.some).sequence
      * res1: Option[List[Int]] = None
      *
      * scala> Vector(1.some, 2.some).sequence
      * res2: Option[scala.collection.immutable.Vector[Int]] = Some(Vector(1, 2))
      * }}}
      */
    def sequence(implicit cbf: CanBuildFrom[Nothing, A, CC[A]]): Option[CC[A]] = {

      val b = {
        val builder = collection.breakOut[CC[Option[A]], A, CC[A]]
        builder(ts)
      }
      b.sizeHint(ts)

      def go(xs: Traversable[Option[A]]): Option[CC[A]] = xs.headOption match {
        case Some(None) => None
        case Some(Some(x)) =>
          b += x
          go(xs.tail)
        case None => b.result.some
      }

      go(ts)
    }
  }
}
