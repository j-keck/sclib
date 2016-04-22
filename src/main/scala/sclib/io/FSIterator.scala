package sclib.io

import java.nio.file.{Files, Path}

import sclib.io.FSEntry.FSEntryImpl
import sclib.ops.all._
import sclib.z._

import scala.util.Try

/**
  * a ''save'' file-system iterator.
  *
  * it wraps every file operation in a `Try`.
  *
  * @example
  *
  * {{{
  * scala>  import sclib.io._
  * scala> val xs = for {
  *      |   wd <- dir("sclib.io")
  *      |   wd <- wd.createTemp
  *      |   pub <- dir(wd, "pub").flatMap(_.create)
  *      |   _ <- file(pub, "a-file").flatMap(_.create)
  *      |   sec <- dir(wd, "sec").flatMap(_.create(0))
  *      |   res = FSIterator(wd).toList
  *      |   _ <- sec.chmod(700)
  *      |   _ <- wd.deleteR
  *      | } yield res
  * xs: scala.util.Try[List[scala.util.Try[sclib.io.FSEntry.FSEntryImpl]]] = Success(List(Success(FSDir(/tmp/sclib.io7738690949969632606/sec)), Failure(java.nio.file.AccessDeniedException: /tmp/sclib.io7738690949969632606/sec), Success(FSDir(/tmp/sclib.io7738690949969632606/pub)), Success(FSFile(/tmp/sclib.io7738690949969632606/pub/a-file))))
  * }}}
  * @param start directory to start
  * @param maxDepth the maximum number of directory levels to visit
  */
case class FSIterator(start: FSDir, maxDepth: Int = Integer.MAX_VALUE) extends Iterator[Try[FSEntryImpl]] {

  private var todo: List[Try[FSEntryImpl]] = ls(start.path)

  override def hasNext: Boolean = todo.nonEmpty

  override def next(): Try[FSEntryImpl] = {
    val curOrErr: Try[FSEntryImpl] = todo.head
    todo = todo.tail

    // if the current entry is a directory, add it to the todos
    curOrErr.foreach{ cur =>
      if(cur.isDirectory){
        todo = ls(cur.path) ++ todo
      }
    }

    curOrErr
  }

  private def ls(path: Path): List[Try[FSEntryImpl]] =
    Try(Files.list(path).toList).fold(_.failure[FSEntryImpl] :: Nil) ( _.map{ p =>
      Try {
        if (Files.isDirectory(p)) new FSDir(p): FSEntryImpl
        else new FSFile(p): FSEntry.FSEntryImpl
      }
    })

}