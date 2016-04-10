package sclib.io

import java.nio.file.{Files, Path}

import sclib.ops.java8._

import scala.util.Try

/**
  * a ''save'' file-system iterator.
  *
  * it wraps every file operation in a `Try`.
  *
  * @example
  * {{{
  * scala> import sclib.io._
  * scala> FSIteratorS(dir("/tmp/d").get)
  * res0: sclib.io.FSIteratorS = non-empty iterator
  * scala>
  * scala> FSIteratorS(dir("/tmp/d").get).foreach(println)
  * Success(FSDir(/tmp/d))
  * Success(FSDir(/tmp/d/c))
  * Success(FSFile(/tmp/d/c/a))
  * Success(FSFile(/tmp/d/c/b))
  * Success(FSFile(/tmp/d/c/c))
  * Failure(java.nio.file.AccessDeniedException: /tmp/d/b)
  * Success(FSDir(/tmp/d/a))
  * Success(FSFile(/tmp/d/a/b))
  * Success(FSFile(/tmp/d/a/a))
  * Success(FSFile(/tmp/d/a/c))
  * }}}
  *
  * @param start directory to start
  * @param maxDepth the maximum number of directory levels to visit
  */
case class FSIteratorS(start: FSDir, maxDepth: Int = Integer.MAX_VALUE) extends Iterator[Try[FSEntry]] {

  private var current: Option[Try[FSEntry]] = None
  private var todo: List[Path] = List(start.path)

  override def hasNext: Boolean = fetchNext()

  override def next(): Try[FSEntry] = {
    val cur = current.getOrElse(throw new NoSuchElementException())
    current = None
    cur
  }

  private def fetchNext(): Boolean = todo.headOption.map { path =>
    todo = todo.tail
    current = Some(Try {
      val entry = FSEntry(path)
      if (Files.isDirectory(path) && entry.depth(start) < maxDepth) {
        todo = Files.list(path).toList ++ todo
      }
      entry
    })
  }.fold(false)(_ => true)
}
