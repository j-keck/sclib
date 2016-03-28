package sclib
package serialization

trait deserialize {

  trait Deserialize[A] {
    def apply: State[String, A]

    protected def next: State[String, String] = State { s =>
      val Array(l, r) = s.split(":", 2)
      r.splitAt(l.toInt)
    }
  }

  object Deserialize {
    def apply[A: Deserialize](s: String): A = Deserialize.apply[A].eval(s)

    def apply[A: Deserialize]: State[String, A] = implicitly[Deserialize[A]].apply
  }

  //
  // primitives
  //

  implicit val stringDes = new Deserialize[String] {
    override def apply: State[String, String] = next
  }

  // FIXME: if i use 'implicit val intDes ..', the compiler throws: 'not found: type $anon'
  implicit def intDes = new Deserialize[Int] {
    override def apply: State[String, Int] = next.map(_.toInt)
  }

  implicit def longDes = new Deserialize[Long] {
    override def apply: State[String, Long] = next.map(_.toLong)
  }

  implicit def doubleDes = new Deserialize[Double] {
    override def apply: State[String, Double] = next.map(_.toDouble)
  }

  implicit def booleanDes = new Deserialize[Boolean] {
    override def apply: State[String, Boolean] = next.map(_.toBoolean)
  }

  //
  // container
  //

  implicit def tupleDes[A: Deserialize, B: Deserialize] = new Deserialize[(A, B)] {
    override def apply: State[String, (A, B)] = for {
      a <- Deserialize[A]
      b <- Deserialize[B]
    } yield (a, b)
  }

  implicit def listDes[A: Deserialize] = new Deserialize[List[A]] {
    override def apply: State[String, List[A]] = next.map(unspool)

    private def unspool(s: String): List[A] = Deserialize[A].run(s) match {
      case (v, "") => List(v)
      case (v, xs) => v :: unspool(xs)
    }
  }

  implicit def vectorDes[A: Deserialize] = new Deserialize[Vector[A]] {
    override def apply: State[String, Vector[A]] = next.map(unspool)

    private def unspool(s: String): Vector[A] = Deserialize[A].run(s) match {
      case (v, "") => Vector(v)
      case (v, xs) => v +: unspool(xs)
    }
  }
}
