package sclib.ops

/**
  * scala.collection.immutable.List extensions
  */
object list {

  object ListOps {
    def unfoldRight[A, B](b: B)(f: B => Option[(A, B)]): List[A] = f(b) match {
      case Some((a, b)) => a :: unfoldRight(b)(f)
      case None => Nil
    }

    def unfoldLeft[A, B](b: B)(f: B => Option[(B, A)]): List[A] = {
      def go(b: B)(l: List[A]): List[A] = f(b) match {
        case Some((b, a)) => go(b)(a :: l)
        case None => l
      }
      go(b)(Nil)
    }

  }

}
