package sclib.io

import java.nio.file._
import scala.collection.JavaConversions._
import scala.util.Try

object FSEntry {
  /**
    * constructs a new `FSEntry`
    *
    * if the given `Path` is a directory, it returns a [[FSDir]], otherwise a [[FSFile]]
    */
  def apply(p: Path): FSEntry =
    if (Files.isDirectory(p)) new FSDir(p)
    else new FSFile(p)
}

/**
  * File System Entry
  */
trait FSEntry {

  /**
    * underlying path
    */
  val path: Path

  /**
    * absolute, normalized path
    */
  lazy val absNormalizedPath = path.toAbsolutePath.normalize


  /**
    * relative depth to a given `FSEntry`
    *
    * @example
    * {{{
    * scala> import sclib.io._
    * scala> for {
    *      |  a <- dir("/tmp")
    *      |  b <- dir("/tmp/a/b")
    *      | } yield a.depth(b)
    * res0: scala.util.Try[Int] = Success(2)
    * }}}
    */
  def depth(other: FSEntry): Int = {
    def elements(p: FSEntry): Int = asScalaIterator(p.absNormalizedPath.iterator()).toList.size
    Math.abs(elements(this) - elements(other))
  }

  /**
    * check if the file exists
    */
  def exists: Try[Boolean] = Try(Files.exists(path))


  /**
    * check if the file is a directory
    */
  def isDirectory: Try[Boolean] = Try(Files.isDirectory(path))


  /**
    * check if the file is a regular file
    */
  def isRegularFile: Try[Boolean] = Try(Files.isRegularFile(path))


  /**
    * file modification time in milliseconds, since the epoch (1970-01-01T00:00:00Z)
    */
  def mtime: Try[Long] = Try(Files.getLastModifiedTime(path).toMillis)


  /**
    * file size in bytes
    */
  def size: Try[Long] = Try(Files.size(path))


  /**
    * delete the file
    *
    * returns a `Failure` if the file doesn't exist.
    *
    * @see [[FSEntry.deleteIfExists]]
    */
  def delete(): Try[Unit] = Try(Files.delete(path))


  /**
    * delete the file if it exists
    *
    * @return `true` if the file was deleted, `false` if it doesn't exist.
    */
  def deleteIfExists(): Try[Boolean] = Try(Files.deleteIfExists(path))


  /**
    * copy the file
    *
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html]]
    */
  def copy(target: FSEntry, options: Seq[CopyOption] = Seq()): Try[FSEntry] = Try {
    Files.copy(path, target.path, options: _*)
    this
  }


  /**
    * move the file
    *
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html]]
    */
  def move(target: FSEntry, options: Seq[CopyOption] = Seq()): Try[FSEntry] = Try {
    Files.move(path, target.path, options: _*)
    this
  }
}
