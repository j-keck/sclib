package sclib.io.fs

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, FileVisitor, Files, Path}

import scala.util.Try
import sclib.ops.all._

/**
  * Represents a 'Directory'
  *
  * ==== to iterate over a directory tree, there are the following functions available: ====
  * ''the method signature from `foreach`, `map` and `flatMap` shown here are simplified''
  *
  *   - [[sclib.io.fs.FSDir.ls:*             ls: Iterator[Try[FSEntryImpl]&#93; ]]
  *   - [[sclib.io.fs.FSDir.foreach[A](tf*   foreach(f: Try[FSEntryImpl] => Unit): Unit ]]
  *   - [[sclib.io.fs.FSDir.foreach[A](tf*   foreach(f: FSEntryImpl => Unit): Try[Unit] ]]
  *   - [[sclib.io.fs.FSDir.map[A](tf*       map[A](f: Try[FSEntryImpl] => A): Iterator[Try[A]&#93; ]]
  *   - [[sclib.io.fs.FSDir.map[A](tf*       map[A](f: FSEntryImpl => A): Iterator[Try[A]&#93; ]]
  *   - [[sclib.io.fs.FSDir.flatMap[A](tf*   flatMap[A](f: Try[FSEntryImpl] => Try[A]): Iterator[Try[A]&#93; ]]
  *   - [[sclib.io.fs.FSDir.flatMap[A](tf*   flatMap[A](f: FSEntryImpl => Try[A]): Iterator[Try[A]&#93; ]]
  *   - [[sclib.io.fs.FSDir.collect[A](pf*   collect[A](pf: PartialFunction[Try[FSEntryImpl]&#93;, A): Iterator[A] ]]
  *
  * this functions doesn't work recursive by default. for a recursive behaviour, use their counterpart functions
  * with the 'R' suffix: `lsR`, `foreachR`, `mapR` or `collectR`.
  *
  * to control the recursive level, you can give the `lsR`, `foreachR`, `mapR` or `collectR` function a 'depth' argument.
  *
  *
  * ====iterate over a directory tree====
  *
  * you can give `foreach`, `map` and `flatMap` a traverse function which receives a `FSEntryImpl` or a `Try[FSEntryImpl]`.
  *
  *
  * assume the following directory tree:
  * <pre>
  * root@main:/tmp/sclib-example # ls -lR /tmp/sclib-example/
  * total 1
  * drwx------  2 j  wheel  5 May  4 10:51 pub
  * d---------  2 j  wheel  5 May  4 10:51 sec
  *
  * /tmp/sclib-example/pub:
  * total 2
  * -rw-r--r--  1 j  wheel  0 May  4 10:51 a
  * -rw-r--r--  1 j  wheel  0 May  4 10:51 b
  * -rw-r--r--  1 j  wheel  0 May  4 10:51 c
  *
  * /tmp/sclib-example/sec:
  * total 1
  * ----------  1 j  wheel  0 May  4 10:51 a
  * </pre>
  *
  * '''fail on first error:'''
  * if you get it a function which receives a `FSEntryImpl` and an exception occurs, the function execution
  * stops and a `Failure` are returned.
  * {{{
  * scala> import sclib.io.fs._
  * scala> dir("/tmp/sclib-example").get.foreachR(println(_: FSEntryImpl))
  * FSDir(/tmp/sclib-example/sec)
  * res0: scala.util.Try[Unit] = Failure(java.nio.file.AccessDeniedException: /tmp/sclib-example/sec)
  * }}}
  *
  *
  * '''exceptions intercepted:'''
  * if you get it a function which receives a `Try[FSEntryImpl]` and an exception occurs, the function execution
  * continues.
  * {{{
  * scala> import sclib.io.fs._
  * scala> import scala.util.Try
  * scala> dir("/tmp/sclib-example").get.foreachR(println(_: Try[FSEntryImpl]))
  * Success(FSDir(/tmp/sclib-example/sec))
  * Failure(java.nio.file.AccessDeniedException: /tmp/sclib-example/sec)
  * Success(FSDir(/tmp/sclib-example/pub))
  * Success(FSFile(/tmp/sclib-example/pub/c))
  * Success(FSFile(/tmp/sclib-example/pub/b))
  * Success(FSFile(/tmp/sclib-example/pub/a))
  * }}}
  *
  *
  * ----
  * ''check the member documentation for examples''
  */
case class FSDir protected[fs](path: Path) extends FSEntry[FSDir] { self =>
  import FSDir._

  override protected[fs] def withPath(p: Path): FSDir = new FSDir(p)

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
    * recursive directory content, start from this directory.
    *
    * @param depth maximum depth level
    * @return a `Iterator` with the directory entries
    */
  def lsR(depth: Int = Integer.MAX_VALUE): Iterator[Try[FSEntryImpl]] = FSIterator(this, depth)

  /**
    * recursive directory content, start from this directory.
    *
    * shortcut to use without parenthesize
    *
    * to control the depth, use [[FSDir.lsR(depth:Int)*]]
    */
  def lsR: Iterator[Try[FSEntryImpl]] = lsR(Integer.MAX_VALUE)
  def ls: Iterator[Try[FSEntryImpl]]  = lsR(1)

  /**
    * apply the given function recursive to every `FSEntry` start from this directory
    */
  def foreachR[A](tf: TraverseFunction[A], depth: Int = Integer.MAX_VALUE): tf.Result = tf(self, depth)
  def foreach[A](tf: TraverseFunction[A]): tf.Result                                  = tf(self, 1)

  /**
    * apply the given function recursive to every `FSEntry` start from this directory
    *
    * @return a `Iterator` where for each entry the given function was applied
    */
  def mapR[A](tf: TraverseFunction[A], depth: Int = Integer.MAX_VALUE): tf.Result = tf(self, depth)
  def map[A](tf: TraverseFunction[A]): tf.Result                                  = tf(self, 1)

  /**
    * apply the given function recursive to every `FSEntry` start from this directory
    *
    * @return a `Iterator` where for each entry the given function was applied
    */
  def flatMapR[A](tf: TraverseFunction[A], depth: Int = Integer.MAX_VALUE): tf.Result = tf(self, depth)
  def flatMap[A](tf: TraverseFunction[A]): tf.Result                                  = tf(self, 1)

  /**
    *
    * @example
    * {{{
    * scala>  import sclib.io.fs._
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
    *      |   val fileNames = wd.collectR{
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
  def collectR[A](pf: PartialFunction[Try[FSEntryImpl], A], depth: Int = Integer.MAX_VALUE): Iterator[A] =
    new Iterator[A] {
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
  def collect[A](pf: PartialFunction[Try[FSEntryImpl], A]): Iterator[A] = collectR(pf, 1)
}
object FSDir {

  trait TraverseFunction[A] {
    type Result
    def apply(dir: FSDir, depth: Int): Result
  }

  object TraverseFunction {
    implicit def foreach(f: FSEntryImpl => Unit) = new TraverseFunction[Unit] {
      override type Result = Try[Unit]
      override def apply(dir: FSDir, depth: Int): Result = Try(FSIterator(dir, depth).foreach(_.fold(throw _)(f)))
    }
    implicit def foreachWithTry(f: Try[FSEntryImpl] => Unit) = new TraverseFunction[Unit] {
      override type Result = Unit
      override def apply(dir: FSDir, depth: Int): Result = FSIterator(dir, depth).foreach(f)
    }
    implicit def map[A](f: FSEntryImpl => A) = new TraverseFunction[A] {
      override type Result = Iterator[Try[A]]
      override def apply(dir: FSDir, depth: Int): Result =
        FSIterator(dir, depth).map(_.fold(_.failure[A])(x => Try(f(x))))
    }
    implicit def mapWithTry[A](f: Try[FSEntryImpl] => A) = new TraverseFunction[A] {
      override type Result = Iterator[Try[A]]
      override def apply(dir: FSDir, depth: Int): Result = FSIterator(dir, depth).map(x => Try(f(x)))
    }
    implicit def flatMap[A](f: FSEntryImpl => Try[A]) = new TraverseFunction[A] {
      override type Result = Iterator[Try[A]]
      override def apply(dir: FSDir, depth: Int): Result = FSIterator(dir, depth).map(_.flatMap(f))
    }
    implicit def flatMapWithTry[A](f: Try[FSEntryImpl] => Try[A]) = new TraverseFunction[A] {
      override type Result = Iterator[Try[A]]
      override def apply(dir: FSDir, depth: Int): Result = FSIterator(dir, depth).map(f)
    }
  }
}
