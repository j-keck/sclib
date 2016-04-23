package sclib.io.fs

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, FileVisitor, Files, Path}

import scala.util.Try

import sclib.io.fs.FSEntry.FSEntryImpl
import sclib.ops.all._

/**
  * Represents a 'Directory'
  *
  * ==== to walk over a directory tree, there are four functions available: ====
  *
  *  - iterate over each entry
  *    {{{ls: Try[Iterator[FSEntryImpl]]}}}
  *
  *  - execute a side effect on each entry
  *    {{{foreach(f: FSEntryImpl => Unit): Try[Unit]}}}
  *
  *  - apply a function on each entry
  *    {{{map[A](f: FSEntryImpl => A): Try[Iterator[A]]}}}
  *
  *  - apply a partial function on each entry on which the function is defined
  *    {{{collectS[A](pf: PartialFunction[Try[FSEntryImpl], A]): Iterator[A]}}}
  *
  *
  * this functions doesn't work recursive by default. to use the recursive behaviour,
  * use their counterpart with the 'R' suffix: `lsR`, `foreachR`, `mapR`, `collectSR`.
  *
  * to control the recursive level, you can give the `ls`, `foreach`, `map` or `collectS` function a 'depth' argument.
  *
  * ----
  *
  * for this functions there exists also a error tolerant counterpart with a 'S' suffix (for '''save'''):
  * `lsS`, `foreachS` and `mapS`. this ''saver'' functions wraps every file operation in a `Try`.
  *
  * ----
  * ''check the member documentation for examples''
  */
case class FSDir protected[fs](path: Path) extends FSEntry[FSDir] { self =>

  /**
    * @see [[FSEntry.createTemp]]
    */
  override def createTemp: Try[FSDir] = Try {
    FSDir(Option(path.getParent).fold(Files.createTempDirectory(name))(Files.createTempDirectory(_, name)))
  }

  /**
    * delete the directory recursive
    *
    * this function is '''NOT''' atomic - when a error occurs, it returns a `Failure` but the
    * successful deleted files are lost.
    */
  def deleteR(): Try[Unit] = Try {
    Files.walkFileTree(path, new FileVisitor[Path] {
      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = throw exc

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = FileVisitResult.CONTINUE

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }

  /**
    * directory content from this directory
    *
    * @param depth maximum depth level - default: 1 for no recursive behavior
    * @return a `Iterator` with the directory entries
    */
  def ls(depth: Int = 1): Try[Iterator[FSEntryImpl]] = Try(FSIterator(this, depth).map(_.fold(throw _)(identity)))

  /**
    * shortcut to use without parenthesize
    *
    * @see [[FSDir.ls(depth:Int*]]
    */
  def ls: Try[Iterator[FSEntryImpl]] = ls()

  /**
    * recursive directory content, start from this directory.
    *
    * to control the depth, use [[FSDir.ls(depth:Int*]].
    */
  def lsR: Try[Iterator[FSEntryImpl]] = ls(Integer.MAX_VALUE)

  /**
    * ''save'' function of [[FSDir.ls(depth:Int*]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.ls(depth:Int*]]
    */
  def lsS(depth: Int = 1): Iterator[Try[FSEntryImpl]] = {
    FSIterator(this, depth)
  }

  /**
    * shortcut to use without parenthesize
    *
    * @see [[FSDir.lsS(depth:Int*]]
    */
  def lsS: Iterator[Try[FSEntryImpl]] = lsS()

  /**
    * ''save'' function of [[FSDir.lsR]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.lsR]]
    */
  def lsSR: Iterator[Try[FSEntryImpl]] = lsS(Integer.MAX_VALUE)

  /**
    * apply the given function to every `FSEntry` in this directory
    *
    * @param depth maximum depth level - default: 1 for no recursive behavior
    */
  def foreach(f: FSEntryImpl => Unit, depth: Int = 1): Try[Unit] = Try {
    FSIterator(this, depth).map(_.fold(throw _)(f)).toList
  }

  /**
    * apply the given function recursive to every `FSEntry` start from this directory
    *
    * to control the depth, use [[FSDir.foreach(f:sclib\.io\.FSEntry\.FSEntryImpl=>Unit*]]
    */
  def foreachR(f: FSEntryImpl => Unit): Try[Unit] = foreach(f, Integer.MAX_VALUE)

  /**
    * ''save'' function of [[FSDir.foreach(f:sclib\.io\.FSEntry\.FSEntryImpl=>Unit*]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.foreach(f:sclib\.io\.FSEntry\.FSEntryImpl=>Unit*]]
    */
  def foreachS(f: Try[FSEntryImpl] => Unit, depth: Int = 1): Unit = {
    FSIterator(this, depth).foreach(f)
  }

  /**
    * ''save'' function of [[FSDir.foreachR]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.foreachR]]
    */
  def foreachSR(f: Try[FSEntry[_]] => Unit): Unit = foreachS(f, Integer.MAX_VALUE)

  /**
    * apply the given function to every `FSEntry` in this directory
    *
    * @param depth maximum depth level - default: 1 for no recursive behavior
    * @return a per `FSEntry` iterator where for each entry the given function was applied
    */
  def map[A](f: FSEntryImpl => A, depth: Int = 1): Try[Iterator[A]] = Try {
    FSIterator(this, depth).map(_.fold(throw _)(f))
  }

  /**
    * apply the given function recursive to every `FSEntry` start from this directory
    *
    * to control the depth, use [[FSDir.map[A](f:sclib\.io\.FSEntry\.FSEntryImpl=>A*]]
    */
  def mapR[A](f: FSEntryImpl => A): Try[Iterator[A]] = map(f, Integer.MAX_VALUE)

  /**
    * ''save'' function of [[FSDir.map[A](f:sclib\.io\.FSEntry\.FSEntryImpl=>A*]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.map[A](f:sclib\.io\.FSEntry\.FSEntryImpl=>A*]]
    */
  def mapS[A](f: Try[FSEntryImpl] => A, depth: Int = 1): Iterator[A] = {
    FSIterator(this, depth).map(f)
  }

  /**
    * ''save'' function of [[FSDir.mapR]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.mapR]]
    */
  def mapSR[A](f: Try[FSEntryImpl] => A): Iterator[A] = mapS(f, Integer.MAX_VALUE)

  /**
    *
    * @example
    * {{{
    * scala> import scala.util.Success
    * scala> import scala.collection.SortedSet
    * scala> dir("wd").flatMap(_.createTemp).map{ wd =>
    *      |   //
    *      |   // create some files - some with a 'f' prefix, others with a 'h' prefix
    *      |   List("f1", "h2", "f3", "h4", "f5").map(file(wd, _).flatMap(_.create))
    *      |   //
    *      |   // create some directories
    *      |   List("fd1", "hd2", "fd3", "hd4", "fd5").map(dir(wd, _).flatMap(_.create))
    *      |   //
    *      |   // collect only files, which doesn't have a 'h' prefix
    *      |   val fileNames = wd.collectSR{
    *      |     case Success(f: FSFile) if ! f.name.startsWith("h") => f.name
    *      |   }.toList
    *      |   //
    *      |   // cleanup
    *      |   wd.deleteR()
    *      |   //
    *      |   fileNames.to[SortedSet]
    *      | }
    * res0: scala.util.Try[SortedSet[String]] = Success(TreeSet(f1, f3, f5))
    * }}}
    */
  def collectS[A](pf: PartialFunction[Try[FSEntryImpl], A], depth: Int = 1): Iterator[A] = new Iterator[A] {
    private val fsIter = new FSIterator(self, depth)

    private var nextElement: Option[A] = None

    override def hasNext: Boolean = {
      fsIter.hasNext && {
        val n = fsIter.next()
        if (pf.isDefinedAt(n)) {
          nextElement = Some(pf(n))
          true
        } else {
          hasNext
        }
      }
    }

    override def next(): A =
      nextElement.map { n =>
        nextElement = None
        n
      }.getOrElse(throw new java.util.NoSuchElementException("next on empty iterator"))
  }

  /**
    * @see [[FSDir.collectS[A](pf*]]
    */
  def collectSR[A](pf: PartialFunction[Try[FSEntryImpl], A]): Iterator[A] = collectS(pf, Integer.MAX_VALUE)
}
