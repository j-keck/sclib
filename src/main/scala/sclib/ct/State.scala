package sclib.ct

/**
  * minimalistic `State` monad
  *
  * @example {{{
  * scala> import sclib.ct._
  *
  * scala> for {
  *      |   a <- State[Int, Int](i => (i, i + 1))
  *      |   b <- State[Int, Int](i => (i, i + 1))
  *      | } yield a -> b
  * res0: sclib.ct.State[Int,(Int, Int)] = State(<function1>)
  *
  * scala> res0.run(0)
  * res1: ((Int, Int), Int) = ((0,1),2)
  * }}}
  */
case class State[S, A](run: S => (A, S)){
  def map[B](f: A => B): State[S, B] = State { s =>
    val (a, s2) = run(s)
    (f(a), s2)
  }

  def flatMap[B](f: A => State[S, B]): State[S, B] = State { s =>
    val (a, s2) = run(s)
    f(a).run(s2)
  }

  //* `run` the `StateMonad` and skip the state */
  def eval(s: S): A = run(s)._1
}