package sclib.ops

object ordering extends ordering

/**
  * `Ordering` extensions
  *
  * ''check the member documentation for examples''
  */
trait ordering {

  /**
    *
    *
    * ===without this import===
    * you have to add an implicit parameter for the 'Ordering' and import the parameter in the function to use it's
    * members (<, <=, >, >=)
    * {{{
    * scala> def isSorted[A](xs: List[A])(implicit ordering: Ordering[A]): Boolean = {
    *      |   import ordering._
    *      |   xs match {
    *      |     case a :: b :: ys => a <= b && isSorted(b :: ys)
    *      |     case _            => true
    *      |   }
    *      | }
    * isSorted: [A](xs: List[A])(implicit ordering: Ordering[A])Boolean
    * }}}
    *
    * ===with this import===
    * only add the context bound for 'Ordering'
    * {{{
    * scala> import sclib.ops.ordering._
    * scala> def isSorted2[A: Ordering](xs: List[A]): Boolean = xs match {
    *      |   case a :: b :: ys => a <= b && isSorted2(b :: ys)
    *      |   case _            => true
    *      | }
    * isSorted2: [A](xs: List[A])(implicit evidence\$1: Ordering[A])Boolean
    * }}}
    */
  implicit class OrderingSyntax[A: Ordering](a: A) {
    def <(b: A)(implicit ord: Ordering[A]) = ord.lt(a, b)
    def >(b: A)(implicit ord: Ordering[A]) = ord.gt(a, b)
    def <=(b: A)(implicit ord: Ordering[A]) = ord.lteq(a, b)
    def >=(b: A)(implicit ord: Ordering[A]) = ord.gteq(a, b)
  }
}
