package sclib.io

import java.io.{BufferedWriter, OutputStreamWriter}
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.StandardOpenOption._
import java.nio.file._

import sclib.ops.java8._

import scala.collection.JavaConversions._
import scala.util.Try

/**
  * Represents a 'File'
  *
  * the functions `write`, `writeLines`, `append` and `appendLines` expects a type-class instance (from [[Writable]])
  * for their to be written payload in scope. instances for `string`, `char`, `short`, `int`, `long`, `float`,
  * `double` and some collection types are already defined.
  *
  * @example
  * {{{
  * scala> import sclib.io._
  * scala> for {
  *      |   fh <- file("/tmp/example")
  *      |   _ <- fh.appendLines("a string")
  *      |   _ <- fh.appendLines(123)
  *      |   _ <- fh.appendLines(1.23)
  *      |   _ <- fh.appendLines(List(1, 2, 3))
  *      |   _ <- fh.append(List(1, 2, 3))
  *      |   content <- fh.slurp
  *      |   _ <- fh.delete
  *      | } yield content
  * res0: scala.util.Try[String] =
  * Success(a string
  * 123
  * 1.23
  * 1
  * 2
  * 3
  * 123)
  * }}}
  *
  * ''check the member documentation for examples''
  */
case class FSFile(path: Path) extends FSEntry {


  /**
    * memory constant operation to process all lines
    *
    * @param cs character set
    * @return a per line iterator
    */
  def lines(cs: Charset = UTF_8): Try[Iterator[String]] = Try {
    Files.lines(path, cs).toIterator
  }


  /** @see [[[sclib.io.FSFile.lines(cs:java\.nio\.charset\.Charset)*]]] */
  def lines: Try[Iterator[String]] = lines()


  /**
    * try to read the whole file at once and return it as a string.
    *
    * use [[[sclib.io.FSFile.lines(cs:java\.nio\.charset\.Charset)*]]] for a memory constant operation.
    *
    * @param cs character set
    * @return whole file content as a string
    */
  def slurp(cs: Charset = UTF_8): Try[String] = Try {
    Files.readAllLines(path).mkString(System.getProperty("line.separator"))
  }


  /** @see [[[sclib.io.FSFile.slurp(cs:java\.nio\.charset\.Charset)*]]] */
  def slurp: Try[String] = slurp()


  /**
    * try to read the whole binary file
    *
    * @return whole file content as a byte array
    */
  def slurpBytes: Try[Array[Byte]] = Try {
    Files.readAllBytes(path)
  }


  /**
    * try to read the file line by line and execute the give function for each line
    *
    * @param f function which are called for each line
    */
  def foreach(f: String => Unit, cs: Charset = UTF_8): Try[Unit] = Try {
    Files.lines(path, cs).forEach(f(_: String))
  }


  /**
    * maps a given function over every line from the given file
    *
    * @param f function which are called for each line
    * @return a per line iterator where for each line the given function was applied
    */
  def map[A](f: String => A, cs: Charset = UTF_8): Try[Iterator[A]] = Try {
    Files.lines(path, cs).map[A](f(_: String)).toIterator
  }


  /**
    * write the given content to the file
    *
    * when the `options` doesn't contain the `APPEND` option, the file are opened
    * with the `TRUNCATE_EXISTING` option which truncate's the file before writing.
    *
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/OpenOption.html]]
    * @param cs      [[http://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html]]
    * @return
    */
  def write[A: Writable](a: A, options: Seq[OpenOption] = Seq(), cs: Charset = UTF_8): Try[FSFile] = Try {

    // truncate the file before writing if append mode is not given
    val _options = options ++ Seq(CREATE, WRITE) ++ (if (options.contains(APPEND)) Seq() else Seq(TRUNCATE_EXISTING))

    val out = Files.newOutputStream(path, _options: _*)
    val writer: BufferedWriter = new BufferedWriter(new OutputStreamWriter(out, cs.newEncoder()))
    Writable(a).foreach { line =>
      writer.append(line)
    }
    writer.close()
    this
  }


  /**
    * write the given content, separated with new-lines to the file
    *
    * @see [[FSFile.write]]
    */
  def writeLines[A: Writable](a: A, options: Seq[OpenOption] = Seq(), cs: Charset = UTF_8): Try[FSFile] = {
    val nl = System.getProperty("line.separator")
    val originalIter = Writable(a)
    // wrap the original iterator in a iterator which add newlines
    write(new Iterator[String] {

      override def hasNext: Boolean = originalIter.hasNext

      override def next(): String = if (originalIter.hasNext) originalIter.next() + nl else originalIter.next()
    }, options, cs)
  }


  /**
    * appends the given content to the file
    *
    * the file are open with the `APPEND` option.
    *
    * @see [[FSFile.write]]
    */
  def append[A: Writable](a: A, options: Seq[OpenOption] = Seq(), cs: Charset = UTF_8): Try[FSFile] =
    write(a, options :+ APPEND, cs)


  /**
    * appends the given content, separated with new-lines to the file
    *
    * the file are open with the `APPEND` option.
    *
    * @see [[FSFile.append]]
    */
  def appendLines[A: Writable](a: A, options: Seq[OpenOption] = Seq(), cs: Charset = UTF_8): Try[FSFile] =
    writeLines(a, options :+ APPEND, cs)


  /**
    * write the given bytes to the file
    *
    * when the `options` doesn't contain the `APPEND` option, the file are opened
    * with the `TRUNCATE_EXISTING` option which truncate's the file before writing.
    *
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/OpenOption.html]]
    */
  def writeBytes(b: Array[Byte], options: Seq[OpenOption] = Seq()): Try[FSFile] = Try {
    // truncate the file before writing if append mode is not given
    val _options = options ++ Seq(CREATE, WRITE) ++ (if (options.contains(APPEND)) Seq() else Seq(TRUNCATE_EXISTING))
    Files.write(path, b, _options: _*)
    this
  }


  /**
    * appends the given bytes to the file
    *
    * the file are open with the `APPEND` option.
    *
    * @see [[FSFile.writeBytes]]
    */
  def appendBytes(b: Array[Byte], options: Seq[OpenOption] = Seq()): Try[FSFile] =
    writeBytes(b, options :+ APPEND)
}
