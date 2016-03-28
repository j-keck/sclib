
package object sclib {

  case class State[S, A](run: S => (A, S)) {
    def map[B](f: A => B): State[S, B] = State { s =>
      val (a, s2) = run(s)
      (f(a), s2)
    }

    def flatMap[B](f: A => State[S, B]): State[S, B] = State { s =>
      val (a, s2) = run(s)
      f(a).run(s2)
    }

    def eval(s: S): A = run(s)._1
  }

}
