package sclib.io.fs

import java.nio.file.{Files, Path}

import scala.util.Try

import sclib.io.fs.FSEntry.FSEntryImpl
import sclib.ops.all._

/**
  * a ''save'' file-system iterator.
  *
  * it wraps every file operation in a `Try`.
  *
  * @example
  * {{{
  * scala> import sclib.io._
  * scala> import sclib.ops.`try`._
  * scala> for {
  *      |   wd <- dir("sclib.io")
  *      |   wd <- wd.createTemp
  *      |   pub <- dir(wd, "pub").flatMap(_.create)
  *      |   _ <- file(pub, "a-file").flatMap(_.create)
  *      |   sec <- dir(wd, "sec").flatMap(_.create(0))
  *      |   names = FSIterator(wd).map(_.fold(_.getClass.getName)(_.name)).toList.sorted
  *      |   _ <- sec.chmod(700)
  *      |   _ <- wd.deleteR
  *      | } yield names
  * res0: scala.util.Try[List[String]] = Success(List(a-file, java.nio.file.AccessDeniedException, pub, sec))
  * }}}
  * @param start directory to start
  * @param maxDepth the maximum number of directory levels to visit
  */
case class FSIterator(start: FSDir, maxDepth: Int = Integer.MAX_VALUE) extends Iterator[Try[FSEntryImpl]] {

  private var todo: List[Try[FSEntryImpl]]   = ls(start.path)
  private var maybePendingDir: Option[FSDir] = None

  override def hasNext: Boolean = {
    // if we have a dir pending, push the content at the top (deep first) of the todo list
    maybePendingDir.foreach { last =>
      todo = ls(last.path) ++ todo
      maybePendingDir = None
    }

    todo.nonEmpty
  }

  override def next(): Try[FSEntryImpl] = {
    val curOrErr: Try[FSEntryImpl] = todo.head
    todo = todo.tail

    curOrErr.foreach { cur =>
      // if the current entry is a directory, and we didn't reach 'maxDepth',
      // save it to process the contents later.
      // "don't look ahead" - necessary for example if we need to fix the permissions before we can look in the dir.
      cur match {
        case d: FSDir if d.depth(start) < maxDepth => maybePendingDir = Some(d)
        case _                                     => // nothing to do
      }
    }

    curOrErr
  }

  private def ls(path: Path): List[Try[FSEntryImpl]] =
    Try(Files.list(path).toList).fold(_.failure[FSEntryImpl] :: Nil)(_.map { p =>
      Try {
        if (Files.isDirectory(p)) new FSDir(p): FSEntryImpl
        else new FSFile(p): FSEntry.FSEntryImpl
      }
    })
}
