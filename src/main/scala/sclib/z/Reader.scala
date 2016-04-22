package sclib.z

/**
  * minimalistic `Reader` monad
  *
  * @example {{{
  * scala> import sclib.z._
  *
  * scala> val action = for {
  *      |   a <- Reader[Int, Int]{_ + 5}
  *      |   b <- Reader[Int, Int]{_ + 10}
  *      | } yield (a, b)
  * action: sclib.z.Reader[Int,(Int, Int)] = Reader(<function1>)
  *
  * scala> action.runReader(1)
  * res0: (Int, Int) = (6,11)
  * }}}
  *
  */
case class Reader[C, A](runReader: C => A) {

  def map[B](f: A => B): Reader[C, B] = Reader { c =>
    f(runReader(c))
  }

  def flatMap[B](f: A => Reader[C, B]): Reader[C, B] = Reader { c =>
    f(runReader(c)).runReader(c)
  }
}
