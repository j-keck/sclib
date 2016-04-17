package sclib

import java.nio.file.Paths

import scala.util.Try

/**
  * io utils
  *
  * all functions are wrapped in a `Try`, so errors are captured and it's easy to compose.
  *
  * @example
  * {{{
  * scala> import sclib.io._
  * scala> for {
  *      |   fh <- file("/tmp/example")
  *      |   _ <- fh.writeLines("1. apple")                        // string
  *      |   _ <- fh.appendLines(List("2. banana", "3. cherry"))   // list of string
  *      |   _ <- fh.append(4)                                     // int
  *      |   _ <- fh.append('.')                                   // char
  *      |   _ <- fh.append(Vector(' ', 'd', 'o', 'g'))            // vector of char
  *      |   content <- fh.slurp
  *      |   _ <- fh.delete
  *      |
  * res0: scala.util.Try[String] =
  * Success(1. apple
  * 2. banana
  * 3. cherry
  * 4. dog)
  * }}}
  *
  * ''check the member documentation for examples''
  */
package object io {

  /**
    * create a file from a given path
    */
  def file(path: String): Try[FSFile] = Try(new FSFile(Paths.get(path)))


  /**
    * create a file under a given directory
    */
  def file(dir: FSDir, path: String): Try[FSFile] = Try(new FSFile(Paths.get(dir.path.toString, path)))


  /**
    * create a directory from a given path
    */
  def dir(path: String): Try[FSDir] = Try(new FSDir(Paths.get(path)))


  /**
    * create a directroy under a given directory
    */
  def dir(dir: FSDir, path: String): Try[FSDir] = Try(new FSDir(Paths.get(dir.path.toString, path)))



  /**
    * type-class for the [[FSFile.write]] / [[FSFile.append]] functions
    */
  trait Writable[A]{
    def apply(a: A): Iterator[String]
  }

  object Writable{
    def apply[A: Writable](a: A) = implicitly[Writable[A]].apply(a)
  }


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

  implicit def listWritable[A: Writable] = new Writable[List[A]]{
    override def apply(a: List[A]): Iterator[String] = a.flatMap(Writable(_)).toIterator
  }

  implicit def vectorWritable[A: Writable] = new Writable[Vector[A]]{
    override def apply(a: Vector[A]): Iterator[String] = a.flatMap(Writable(_)).toIterator
  }
}
