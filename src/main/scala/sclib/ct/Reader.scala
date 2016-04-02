package sclib.ct

case class Reader[C, A](runReader: C => A) {

  def map[B](f: A => B): Reader[C, B] = Reader { c =>
    f(runReader(c))
  }

  def flatMap[B](f: A => Reader[C, B]): Reader[C, B] = Reader { c =>
    f(runReader(c)).runReader(c)
  }
}
