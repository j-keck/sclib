package sclib.io.fs

import java.nio.file.{Files, Path}

import scala.util.Try

import sclib.ops.all._

/**
  * a ''save'' file-system iterator.
  *
  * it wraps every file operation in a `Try`.
  *
  *
  * by default, the iterator start ''in'' the given directory, and walk recursively (deep first) over all entries.
  * you can control the recursive level with the 'depth' argument, where 1 means only the content from the given directory.
  * to start ''with'' the given directory, use 'includeStartDir = true'.
  *
  *
  * assume the following directory tree:
  * <pre>
  * /tmp/sclib-example/a1/b1/a1b1file
  * /tmp/sclib-example/a1/b1/c1/a1b1c1file
  * /tmp/sclib-example/a2/b2/a2b2file
  * </pre>
  *
  *
  * ===default behaviour===
  * <pre>
  * scala> FSIterator(dir("/tmp/sclib-example").get).foreach(println)
  * Success(FSDir(/tmp/sclib-example/a1))
  * Success(FSDir(/tmp/sclib-example/a1/b1))
  * Success(FSFile(/tmp/sclib-example/a1/b1/a1b1file))
  * Success(FSDir(/tmp/sclib-example/a1/b1/c1))
  * Success(FSFile(/tmp/sclib-example/a1/b1/c1/a1b1c1file))
  * Success(FSDir(/tmp/sclib-example/a2))
  * Success(FSDir(/tmp/sclib-example/a2/b2))
  * Success(FSFile(/tmp/sclib-example/a2/b2/a2b2file))
  * </pre>
  *
  * ===with max-depth: 2 and the start directory included===
  * <pre>
  * scala> FSIterator(dir("/tmp/sclib-example").get, depth = 2, includeStartDir = true).foreach(println)
  * Success(FSDir(/tmp/sclib-example))
  * Success(FSDir(/tmp/sclib-example/a1))
  * Success(FSDir(/tmp/sclib-example/a1/b1))
  * Success(FSDir(/tmp/sclib-example/a2))
  * Success(FSDir(/tmp/sclib-example/a2/b2))
  * </pre>
  *
  *
  * @param start directory to start
  * @param depth the maximum number of directory levels to visit
  */
case class FSIterator(start: FSDir, depth: Int = Integer.MAX_VALUE, includeStartDir: Boolean = false)
    extends Iterator[Try[FSEntryImpl]] {

  private var todo: List[Try[FSEntryImpl]]   = if (includeStartDir) List(start.success) else ls(start.path)
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

    curOrErr.foreach {
      // if the current entry is a directory, and we didn't reach 'maxDepth',
      // save it to process the contents later.
      // "don't look ahead" - necessary for example if we need to fix the permissions before we can look in the dir.
      case d: FSDir if d.depth(start) < depth => maybePendingDir = Some(d)
      case _                                  => // nothing to do
    }

    curOrErr
  }

  private def ls(path: Path): List[Try[FSEntryImpl]] =
    Try(Files.list(path).toList).fold(_.failure[FSEntryImpl] :: Nil)(_.map { p =>
      Try {
        if (Files.isDirectory(p)) new FSDir(p): FSEntryImpl
        else new FSFile(p): FSEntryImpl
      }
    })
}
