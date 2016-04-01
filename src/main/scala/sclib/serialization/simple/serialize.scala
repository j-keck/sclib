package sclib.serialization.simple

trait serialize {

  trait Serialize[A] {
    def apply(a: A): String

    protected def pack(s: String): String = s"${s.length}:${s}"
  }

  object Serialize {
    def apply[A: Serialize](a: A): String = implicitly[Serialize[A]].apply(a)
  }

  //
  // primitives
  //

  implicit val stringSer = new Serialize[String] {
    override def apply(a: String): String = pack(a)
  }

  implicit val charSer = new Serialize[Char] {
    override def apply(a: Char): String = pack(a.toString)
  }

  implicit val intSer = new Serialize[Int] {
    override def apply(a: Int): String = pack(a.toString)
  }

  implicit val longSer = new Serialize[Long] {
    override def apply(a: Long): String = pack(a.toString)
  }

  implicit val doubleSer = new Serialize[Double] {
    override def apply(a: Double): String = pack(a.toString)
  }

  implicit val booleanSer = new Serialize[Boolean] {
    override def apply(a: Boolean): String = pack(a.toString)
  }

  //
  // container
  //

  implicit def optionSer[A: Serialize] = new Serialize[Option[A]]{
    override def apply(a: Option[A]): String = pack(a.fold("N")(s => Serialize(s)))
  }

  implicit def eitherSer[A: Serialize, B: Serialize] = new Serialize[Either[A, B]]{
    override def apply(a: Either[A, B]): String = pack(a.fold("L" + Serialize(_), "R" + Serialize(_)))
  }

  implicit def tupleSer[A: Serialize, B: Serialize] = new Serialize[(A, B)] {
    override def apply(a: (A, B)): String = Serialize(a._1) + Serialize(a._2)
  }

  implicit def listSer[A: Serialize] = new Serialize[List[A]] {
    override def apply(a: List[A]): String = pack(a.map(Serialize[A]).mkString)
  }

  implicit def vectorSer[A: Serialize] = new Serialize[Vector[A]]{
    override def apply(a: Vector[A]): String = pack(a.map(Serialize[A]).mkString)
  }

  implicit def setSer[A: Serialize] = new Serialize[Set[A]]{
    override def apply(a: Set[A]): String = pack(a.map(Serialize[A]).mkString)
  }

  implicit def mapSer[A: Serialize, B: Serialize] = new Serialize[Map[A, B]]{
    override def apply(m: Map[A, B]): String = pack(m.foldRight("")(Serialize[Tuple2[A, B]](_) + _))
  }
}
