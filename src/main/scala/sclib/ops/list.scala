package sclib.ops

object list extends list
/**
  * `List` extensions
  *
  * ''check the member documentation for examples''
  */
trait list {

  /**
    * utility's for `List`
    */
  object ListOps {

    /**
      * `unfoldRight` is a `dual` for `foldRight`.
      *
      * @example {{{
      * scala> import sclib.ops.list._
      * scala> ListOps.unfoldRight(0){ i =>
      *      |   if(i > 10) None else Some(i, i + 1)
      *      | }
      * res0: List[Int] = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      * }}}
      */
    def unfoldRight[A, B](b: B)(f: B => Option[(A, B)]): List[A] = f(b) match {
      case Some((a, b)) => a :: unfoldRight(b)(f)
      case None => Nil
    }

    /**
      * `unfoldLeft` is a `dual` for `foldLeft`.
      *
      * @example {{{
      * scala> import sclib.ops.list._
      * scala> ListOps.unfoldLeft(0){ i =>
      * |   if(i > 10) None else Some(i+1, i)
      * | }
      * res0: List[Int] = List(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
      * }}}
      */
    def unfoldLeft[A, B](b: B)(f: B => Option[(B, A)]): List[A] = {
      def go(b: B)(l: List[A]): List[A] = f(b) match {
        case Some((b, a)) => go(b)(a :: l)
        case None => l
      }
      go(b)(Nil)
    }
  }


  /**
    * extensions on `List` instances
    */
  implicit class ListOps[A](l: List[A]) {

    /**
      * partition a list into sub-lists - start by the given predicate
      *
      * @example
      * {{{
      * scala> import sclib.ops.list._
      * scala> val l = List("-- heading1", "a", "b", "-- heading2", "c", "d")
      * scala> l.partitionsBy(_.startsWith("--"))
      * res0: List[List[String]] = List(List(-- heading1, a, b), List(-- heading2, c, d))
      * }}}
      */
    def partitionsBy(p: A => Boolean): List[List[A]] = {
      def go(xs: List[A], acc: List[A]): List[List[A]] = xs match {
        case Nil               => acc.reverse :: Nil
        case (y :: ys) if p(y) => acc.reverse :: go(ys, List(y))
        case (y :: ys)         => go(ys, y :: acc)
      }

      if (l.nonEmpty) go(l.tail, List(l.head))
      else Nil
    }
  }

}
