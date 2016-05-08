package sclib

import sclib.z.{EitherT, Reader}

/**
  * pattern's
  *
  * ''check the member documentation for examples''
  */
package object patterns {

  /**
    * simple '''AppF'''unction
    *
    * @example {{{
    * scala> import sclib.patterns._
    * scala> import sclib.ops.either._
    *
    * scala> val action = for {
    *      |   a <- AppF{i: Int => i.right[String]}
    *      |   b <- AppF{i: Int => if(i < 5) (i * 10).right else "BOOM".left}
    *      |   c <- AppF.lift(33.right[String])
    *      | } yield (a, b, c)
    * action: sclib.z.EitherT[[B]sclib.z.Reader[Int,B],String,(Int, Int, Int)] = EitherT(Reader(<function1>))
    *
    * scala> action.runEitherT.runReader(2)
    * res0: Either[String,(Int, Int, Int)] = Right((2,20,33))
    *
    * scala> action.runEitherT.runReader(8)
    * res1: Either[String,(Int, Int, Int)] = Left(BOOM)
    * }}}
    */
  object AppF {
    def apply[C, A, B](f: C => Either[A, B]) =
      EitherT[Reader[C, ?], A, B](Reader(f))

    /** lift a Either into AppF */
    def lift[C, A, B](f: => Either[A, B]) = apply[C, A, B](_ => f)
  }
}
