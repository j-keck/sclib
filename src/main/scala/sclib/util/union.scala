package sclib.util

object union extends union

/**
  * simple union type
  *
  * [[http://stackoverflow.com/questions/3508077/how-to-define-type-disjunction-union-types#comment8603587_6883076]]
  *
  * @example {{{
  * scala> import sclib.util.union._
  * scala> def f[A: (Int Or String)#Check](a: A): Int = a match {
  *      |   case i: Int => i
  *      |   case s: String => s.length
  *      | }
  * scala> f(5)
  * res0: Int = 5
  * scala> f("hey")
  * res1: Int = 3
  * // this throws a compiler error:
  * scala> f(5L)
  * }}}
  */
trait union {

  trait Contra[-A]

  type Union[A, B] = {
    type Check[Z] = Contra[Contra[Z]] <:< Contra[Contra[A] with Contra[B]]
  }

  /**
    * alias for [[Union]] - for inline use
    */
  type Or[A, B] = Union[A, B]
}