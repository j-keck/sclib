package sclib

import sclib.ct.{EitherT, Reader}

/**
  * "design-pattern's"
  */
package object dp {

  /**
    * simple '''AppF'''unction
    */
  object AppF {
    def apply[C, A, B](f: C => Either[A, B]) =
      EitherT[({type L[B] = Reader[C, B]})#L, A, B](Reader(f))

    /** lift a Either into AppF */
    def lift[C, A, B](f: => Either[A, B]) = apply[C, A, B](_ => f)
  }
}
