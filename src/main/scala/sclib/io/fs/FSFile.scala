package sclib.io.fs

import java.io.{BufferedWriter, OutputStreamWriter}
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, OpenOption, Path}

import scala.util.Try
import scala.collection.JavaConverters._

import sclib.io.autoClose
import sclib.ops.all._

/**
  * Represents a 'File'
  *
  * the functions `write`, `writeLines`, `append` and `appendLines` expects a type-class instance (from [[Writable]])
  * for their to be written payload in scope. instances for `string`, `char`, `short`, `int`, `long`, `float`,
  * `double` and some collection types are already defined (see [[Writable$]]).
  *
  * the `write` and `append` functions don't add a newline at the end / between the sequences.
  * for functions which add newlines use `writeLines` and `appendLines`.
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
case class FSFile protected[io](path: Path) extends FSEntry[FSFile] {

  override protected[fs] def withPath(p: Path): FSFile = new FSFile(p)

  /**
    * memory constant operation to process all lines
    *
    * @param cs character set
    * @return a per line iterator
    */
  def lines(cs: Charset = UTF_8): Try[Iterator[String]] = Try {
    Files.lines(path, cs).toIterator
  }

  /** @see [[[sclib.io.fs.FSFile.lines(cs:java\.nio\.charset\.Charset)*]]] */
  def lines: Try[Iterator[String]] = lines()

  /**
    * try to read the whole file at once and return it as a string.
    *
    * use [[[sclib.io.fs.FSFile.lines(cs:java\.nio\.charset\.Charset)*]]] for a memory constant operation.
    *
    * @param cs character set
    * @return whole file content as a string
    */
  def slurp(cs: Charset = UTF_8): Try[String] = Try {
    Files.readAllLines(path).asScala.mkString(System.getProperty("line.separator"))
  }

  /** @see [[[sclib.io.fs.FSFile.slurp(cs:java\.nio\.charset\.Charset)*]]] */
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
    val _options =
      options ++ Seq(CREATE, WRITE) ++ (if (options.contains(APPEND)) Seq()
                                        else Seq(TRUNCATE_EXISTING))

    for {
      out    <- autoClose(Files.newOutputStream(path, _options: _*))
      writer <- autoClose(new BufferedWriter(new OutputStreamWriter(out, cs.newEncoder)))
    } Writable(a).foreach { line =>
      writer.append(line)
    }

    this
  }

  /**
    * write the given content, separated with new-lines to the file
    *
    * @see [[FSFile.write]]
    */
  def writeLines[A: Writable](a: A, options: Seq[OpenOption] = Seq(), cs: Charset = UTF_8): Try[FSFile] = {
    val nl           = System.getProperty("line.separator")
    val originalIter = Writable(a)
    // wrap the original iterator in a iterator which add newlines
    write(new Iterator[String] {

      override def hasNext: Boolean = originalIter.hasNext

      override def next(): String =
        if (originalIter.hasNext) originalIter.next() + nl
        else originalIter.next()
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
    val _options =
      options ++ Seq(CREATE, WRITE) ++ (if (options.contains(APPEND)) Seq()
                                        else Seq(TRUNCATE_EXISTING))
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
