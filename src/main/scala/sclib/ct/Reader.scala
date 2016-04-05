package sclib.ct

/**
  * minimalistic `Reader` monad
  *
  * @example {{{
  * scala> import sclib.ct._
  *
  * scala> for {
  *      |   a <- Reader[Int, Int]{_ + 5}
  *      |   b <- Reader[Int, Int]{_ + 10}
  *      | } yield (a, b)
  * res0: sclib.ct.Reader[Int,(Int, Int)] = Reader(<function1>)
  *
  * scala> res0.runReader(1)
  * res1: (Int, Int) = (6,11)
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
