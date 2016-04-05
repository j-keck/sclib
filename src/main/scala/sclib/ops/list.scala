package sclib.ops

/**
  * `List` extensions
  *
  * ''check the member documentation for examples''
  */
object list {

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

}
