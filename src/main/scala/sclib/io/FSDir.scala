package sclib.io

import java.io.IOException
import java.nio.file.attribute.{BasicFileAttributes, FileAttribute}
import java.nio.file._

import sclib.ops.java8._

import scala.util.Try

/**
  * Represents a 'Directory'
  *
  * ==== to walk over the directory tree, there are three functions available: ====
  *
  *   - `ls: Try[Iterator[FSEntry]]`: to get a iterator
  *   - `foreach(f: FSEntry => Unit): Try[Unit]`: to execute a side effect
  *   - `map[A](f: FSEntry => A): Try[Iterator[A]]`: to map the result
  *
  * this functions doesn't work recursive by default. to use the recursive behaviour,
  * use their counterpart with the 'R' suffix: `lsR`, `foreachR` or `mapR`.
  *
  * to control the recursive level, you can give the `ls`, `foreach` or `map` function a 'depth' argument.
  *
  * ----
  *
  * for this functions there exists also a error tolerant counterpart with a 'S' suffix (for '''save'''):
  * `lsS`, `foreachS` and `mapS`. this ''saver'' functions wraps every file operation in a `Try`.
  *
  * ----
  * ''check the member documentation for examples''
  */
case class FSDir(path: Path) extends FSEntry {


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
    * create the directories
    *
    * @param attrs [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/PosixFileAttributes.html]]
    */
  def mkDirs(attrs: Seq[FileAttribute[_]] = Seq()): Try[FSDir] = Try {
    Files.createDirectories(path, attrs: _*)
    this
  }


  /**
    * @see [[FSDir.mkDirs(attrs*]]
    */
  def mkDirs: Try[FSDir] = mkDirs()



  /**
    * directory content from this directory
    *
    * @param depth maximum depth level - default: 1 for no recursive behavior
    * @return a `Iterator` with the directory entries
    */
  def ls(depth: Int = 1): Try[Iterator[FSEntry]] = Try {
    Files.walk(path, depth).map[FSEntry](FSEntry(_: Path)).toIterator
  }


  /**
    * @see [[FSDir.ls(depth:Int*]]
    */
  def ls: Try[Iterator[FSEntry]] = ls()


  /**
    * recursive directory content, start from this directory.
    *
    * to control the depth, use [[FSDir.ls(depth:Int*]].
    */
  def lsR: Try[Iterator[FSEntry]] = ls(Integer.MAX_VALUE)


  /**
    * ''save'' function of [[FSDir.ls(depth:Int*]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.ls(depth:Int*]]
    */
  def lsS(depth: Int = 1): Iterator[Try[FSEntry]] = {
    FSIteratorS(this, depth)
  }

  /**
    * ''save'' function of [[FSDir.lsR]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.lsR]]
    */
  def lsSR: Iterator[Try[FSEntry]] = lsS(Integer.MAX_VALUE)


  /**
    * apply the given function to every `FSEntry` in this directory
    *
    * @param depth maximum depth level - default: 1 for no recursive behavior
    */
  def foreach(f: FSEntry => Unit, depth: Int = 1): Try[Unit] = Try {
    Files.walk(path, depth).forEach { p: Path => f(FSEntry(p)) }
  }


  /**
    * apply the given function recursive to every `FSEntry` start from this directory
    *
    * to control the depth, use [[FSDir.foreach(f:sclib\.io\.FSEntry=>Unit*]]
    */
  def foreachR(f: FSEntry => Unit): Try[Unit] = foreach(f, Integer.MAX_VALUE)


  /**
    * ''save'' function of [[FSDir.foreach(f:sclib\.io\.FSEntry=>Unit*]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.foreach(f:sclib\.io\.FSEntry=>Unit*]]
    */
  def foreachS(f: Try[FSEntry] => Unit, depth: Int = 1): Unit = {
    FSIteratorS(this, depth).foreach(f)
  }


  /**
    * ''save'' function of [[FSDir.foreachR]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.foreachR]]
    */
  def foreachSR(f: Try[FSEntry] => Unit): Unit = foreachS(f, Integer.MAX_VALUE)



  /**
    * apply the given function to every `FSEntry` in this directory
    *
    * @param depth maximum depth level - default: 1 for no recursive behavior
    * @return a per `FSEntry` iterator where for each entry the given function was applied
    */
  def map[A](f: FSEntry => A, depth: Int = 1): Try[Iterator[A]] = Try {
    Files.walk(path, depth).map[A] { p: Path => f(FSEntry(p)) }.toIterator
  }


  /**
    * apply the given function recursive to every `FSEntry` start from this directory
    *
    * to control the depth, use [[FSDir.map[A](f:sclib\.io\.FSEntry=>A*]]
    */
  def mapR[A](f: FSEntry => A): Try[Iterator[A]] = map(f, Integer.MAX_VALUE)


  /**
    * ''save'' function of [[FSDir.map[A](f:sclib\.io\.FSEntry=>A*]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.map[A](f:sclib\.io\.FSEntry=>A*]]
    */
  def mapS[A](f: Try[FSEntry] => A, depth: Int = 1): Iterator[A] = {
    FSIteratorS(this, depth).map(f)
  }


  /**
    * ''save'' function of [[FSDir.mapR]] - wraps every file operation in a `Try`.
    *
    * @see [[FSDir.mapR]]
    */
  def mapSR[A](f: Try[FSEntry] => A): Iterator[A] = mapS(f, Integer.MAX_VALUE)

}
