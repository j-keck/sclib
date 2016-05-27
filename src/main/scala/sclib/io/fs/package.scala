package sclib.io

import java.nio.file.{Path, Paths}

import sclib.ops.all._

import scala.util.Try

/**
  * working with files and directories
  *
  * ===get a file / directory handle===
  *
  * to get a file handle, use any of the following functions:
  *   - [[fs.file(name:String)* file(name: String): Try[FSFile&#93;]]
  *   - [[fs.file(parent:sclib\.io\.fs\.FSDir,name:String)* file(parent: FSDir, name: String): Try[FSFile&#93;]]
  *   - [[fs.file(path:java\.nio\.file\.Path)* file(path: Path): Try[FSFile&#93;]]
  *
  * for a directory handle:
  *   - [[fs.dir(name:String)* dir(name: String): Try[FSDir&#93;]]
  *   - [[fs.dir(parent:sclib\.io\.fs\.FSDir,name:String)* dir(parent: FSDir, name: String): Try[FSDir&#93;]]
  *   - [[fs.dir(path:java\.nio\.file\.Path)* dir(path: Path): Try[FSDir&#93;]]
  *
  *
  * ======example:======
  * {{{
  * scala> import sclib.io.fs._
  * scala> file("/path/file-name")
  * res0: scala.util.Try[FSFile] = Success(FSFile(/path/file-name))
  * scala> dir("/path/dir-name")
  * res1: scala.util.Try[FSDir] = Success(FSDir(/path/dir-name))
  * }}}
  *
  *
  * this functions returns a `Failure` if the requested entry exists, but has a wrong type.
  * {{{
  * scala> import sclib.io.fs._
  * scala> file("a-file").flatMap(_.create)
  * res0: scala.util.Try[FSFile] = Success(FSFile(a-file))
  * scala> dir("a-file")
  * res1: scala.util.Try[FSDir] = Failure(java.lang.Exception: 'a-file' is a file)
  * scala> file("a-file").flatMap(_.delete)
  * res2: scala.util.Try[Unit] = Success(())
  * }}}
  *
  *
  * ===work with a handle===
  *
  * all functions which can throw a exception are wrapped in a `Try`, so it's easy to compose.
  * {{{
  * scala> import sclib.io.fs._
  * scala> for {
  *      |   fh <- file("file-name")
  *      |   _ <- fh.write("file content")
  *      |   c <- fh.slurp
  *      |   _ <- fh.delete
  *      | } yield c
  * res0: scala.util.Try[String] = Success(file content)
  * }}}
  *
  * @see [[sclib.io.fs.FSFile]]
  * @see [[sclib.io.fs.FSDir]]
  */
package object fs {

  /**
    * initialize a file-handle from a given path
    *
    * if the given path exists, and is a directory, a failure are returned.
    *
    * ''this doesn't create the file - use [[FSEntry.create(n:*]] / [[FSFile.createTemp]] / any write method to create it''
    */
  def file(path: Path): Try[FSFile] = {
    val f = new FSFile(path)
    if (f.exists && f.isDirectory) s"'${path}' is a directory".failure
    else f.success
  }

  /**
    * initialize a file-handle from a given path
    *
    * if the given path exists, and is a directory, a failure are returned.
    *
    * ''this doesn't create the file - use [[FSEntry.create(n:*]] / [[FSFile.createTemp]] / any write method to create it''
    */
  def file(name: String): Try[FSFile] = file(Paths.get(name))

  /**
    * initialize a file-handle from a given path
    *
    * if the given path exists, and is a directory, a failure are returned.
    *
    * ''this doesn't create the file - use [[FSEntry.create(n:*]] / [[FSFile.createTemp]] / any write method to create it''
    */
  def file(parent: FSDir, name: String): Try[FSFile] = file(Paths.get(parent.path.toString, name))

  /**
    * initialize a directory-handle from a given path
    *
    * if the given path exists, and is a file, a failure are returned.
    *
    * ''this doesn't create the directory - use [[FSEntry.create(n:*]] / [[FSDir.createTemp]] to create it''
    */
  def dir(path: Path): Try[FSDir] = {
    val d = new FSDir(path)
    if (d.exists && d.isRegularFile) s"'${path}' is a file".failure
    else d.success
  }

  /**
    * initialize a directory-handle from a given path
    *
    * if the given path exists, and is a file, a failure are returned.
    *
    * ''this doesn't create the directory - use [[FSEntry.create(n:*]] / [[FSDir.createTemp]] to create it''
    */
  def dir(name: String): Try[FSDir] = dir(Paths.get(name))

  /**
    * initialize a directory-handle from a given path
    *
    * if the given path exists, and is a file, a failure are returned.
    *
    * ''this doesn't create the directory - use [[FSEntry.create(n:*]] / [[FSDir.createTemp]] to create it''
    */
  def dir(parent: FSDir, name: String): Try[FSDir] = dir(Paths.get(parent.path.toString, name))

  /**
    * Any FSEntry Implementation
    */
  type FSEntryImpl = FSEntry[A] forSome { type A <: FSEntry[A] }
}
