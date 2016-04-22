package sclib.z

/**
  * minimalistic `State` monad
  *
  * @example {{{
  * scala> import sclib.z._
  *
  * scala> val action = for {
  *      |   a <- State[Int, Int](i => (i, i + 1))
  *      |   b <- State[Int, Int](i => (i, i + 1))
  *      | } yield a -> b
  * action: sclib.z.State[Int,(Int, Int)] = State(<function1>)
  *
  * scala> action.run(0)
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