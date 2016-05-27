package sclib.io.fs

/**
  * type-class for the [[FSFile.write]] / [[FSFile.append]] functions
  */
trait Writable[A] {
  def apply(a: A): Iterator[String]
}

/**
  * Instances for the [[Writable]] type-class
  */
object Writable {
  def apply[A: Writable](a: A) = implicitly[Writable[A]].apply(a)

  implicit val stringWritable = new Writable[String] {
    override def apply(a: String): Iterator[String] = Iterator.single(a)
  }

  implicit val charWritable = new Writable[Char] {
    override def apply(a: Char): Iterator[String] = Iterator.single(a.toString)
  }

  implicit val shortWritable = new Writable[Short] {
    override def apply(a: Short): Iterator[String] = Iterator.single(a.toString)
  }

  implicit val intWritable = new Writable[Int] {
    override def apply(a: Int): Iterator[String] = Iterator.single(a.toString)
  }

  implicit val longWritable = new Writable[Long] {
    override def apply(a: Long): Iterator[String] = Iterator.single(a.toString)
  }

  implicit val floatWritable = new Writable[Float] {
    override def apply(a: Float): Iterator[String] = Iterator.single(a.toString)
  }

  implicit val doubleWritable = new Writable[Double] {
    override def apply(a: Double): Iterator[String] = Iterator.single(a.toString)
  }

  implicit def iteratorWritable[A: Writable] = new Writable[Iterator[A]] {
    override def apply(a: Iterator[A]): Iterator[String] = a.flatMap(Writable[A](_))
  }

  implicit def seqWritable[A: Writable] = new Writable[Seq[A]] {
    override def apply(a: Seq[A]): Iterator[String] = a.flatMap(Writable(_)).toIterator
  }

  implicit def listWritable[A: Writable] = new Writable[List[A]] {
    override def apply(a: List[A]): Iterator[String] = a.flatMap(Writable(_)).toIterator
  }

  implicit def vectorWritable[A: Writable] = new Writable[Vector[A]] {
    override def apply(a: Vector[A]): Iterator[String] = a.flatMap(Writable(_)).toIterator
  }
}
