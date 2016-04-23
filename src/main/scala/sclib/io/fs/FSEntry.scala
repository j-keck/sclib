package sclib.io.fs

import java.nio.file.{CopyOption, Files, Path}
import java.nio.file.attribute.{FileAttribute, FileTime, PosixFilePermission, PosixFilePermissions}
import java.nio.file.attribute.PosixFilePermission._

import scala.util.Try
import scala.collection.JavaConversions._
import sclib.io.fs.FSEntry.FSEntryImpl

/**
  * File System Entry
  */
trait FSEntry[Self <: FSEntry[Self]] { self: Self =>

  /**
    * underlying path
    */
  val path: Path

  /**
    * absolute, normalized path
    */
  lazy val absNormalizedPath: Path = path.toAbsolutePath.normalize

  /**
    * file / directory name
    */
  lazy val name: String = path.getFileName.toString

  /**
    * create the filesystem entry with default permissions
    */
  def create: Try[Self] = this match {
    case _: FSFile =>
      create(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ)
    case _: FSDir =>
      create(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE)
  }

  /**
    * create the filesystem entry with the given permissions
    *
    * @param perms
    * @return
    */
  def create(perms: PosixFilePermission*): Try[Self] = Try {
    this match {
      case _: FSFile =>
        Files.createFile(path, PosixFilePermissions.asFileAttribute(setAsJavaSet(perms.toSet)))
      case _: FSDir =>
        Files.createDirectory(path, PosixFilePermissions.asFileAttribute(setAsJavaSet(perms.toSet)))
    }
    this
  }

  /**
    * create the filesystem entry with the given permissions
    *
    * @example
    * {{{
    * scala> import scala.collection.SortedSet
    * scala> for {
    *      |   wd <- dir("/tmp/sclib")
    *      |   wd <- wd.createTemp
    *      |   fh <- file(wd, "example")
    *      |   _ <- fh.create(740)
    *      |   perm <- fh.lsmod
    *      |   _ <- wd.deleteR
    *      | } yield perm.to[SortedSet]
    * res0: scala.util.Try[SortedSet[java.nio.file.attribute.PosixFilePermission]] = Success(TreeSet(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ))
    * }}}
    * @param n
    */
  def create(n: Int): Try[Self] = FSPerm.calc(n).flatMap(create(_:_*))

  /**
    * create the filesystem entry with the given permissions
    *
    * @example
    * {{{
    * create("a=r,u+w") -> create the entry with the PosixFilePermissions: OWNER_READ, OWNER_WRITE, GROUP_READ and OTHERS_READ
    *
    * scala> import scala.collection.SortedSet
    * scala> for {
    *      |   wd <- dir("/tmp/sclib")
    *      |   wd <- wd.createTemp
    *      |   fh <- file(wd, "example")
    *      |   _ <- fh.create("a=r,u+wx")
    *      |   perm <- fh.lsmod
    *      |   _ <- wd.deleteR
    *      | } yield perm.to[SortedSet]
    * res0: scala.util.Try[scala.collection.SortedSet[java.nio.file.attribute.PosixFilePermission]] = Success(TreeSet(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, OTHERS_READ))
    * }}}
    */
  def create(mode: String): Try[Self] =
    for {
      newPerms    <- FSPerm.mod(Seq(), mode)
      res         <- create(newPerms:_*)
    } yield res

  /**
    * create a new temporary filesystem entry.
    * the actual name is used for the prefix
    *
    * @example
    * {{{
    * scala> for {
    *      |   fh <- dir("/tmp/a-temp-dir")
    *      |   // override the original fh
    *      |   fh <- fh.createTemp
    *      |   _ <- fh.delete
    *      |   // fh.name is something like 'a-temp-dir7526811586167481701'
    *      | } yield fh.name.matches("a-temp-dir\\d+")
    * res0: scala.util.Try[Boolean] = Success(true)
    *
    *
    * scala> for {
    *      |   fh <- file("/tmp/a-temp-file")
    *      |   // override the original fh
    *      |   fh <- fh.createTemp
    *      |   _ <- fh.delete
    *      |   // fh.name is something like 'a-temp-file9014711075515420555'*
    *      | } yield fh.name.matches("a-temp-file\\d+")
    * res0: scala.util.Try[Boolean] = Success(true)
    *
    * scala> for {
    *      |   fh <- file("/tmp/a-temp-file.txt")
    *      |   // override the original fh
    *      |   fh <- fh.createTemp
    *      |   _ <- fh.delete
    *      |   // fh.name is something like 'a-temp-file2068553337840465580.txt'
    *      | } yield fh.name.matches("a-temp-file\\d+\\.txt")
    * res1: scala.util.Try[Boolean] = Success(true)
    * }}}
    */
  def createTemp: Try[Self]

  /**
    * create the directories
    *
    * - when called on a `FSDir`, the directroy's path (inclusive parents) are created
    * - when called on a `FSFile` the parent hierarchy are created
    *
    * @param attrs [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/PosixFileAttributes.html]]
    */
  def mkDirs(attrs: Seq[FileAttribute[_]] = Seq()): Try[Self] = Try {
    val p = this match {
      case _: FSDir  => path
      case _: FSFile => absNormalizedPath.getParent
    }
    Files.createDirectories(p, attrs:_*)
    this
  }

  /**
    * @see [[FSEntry.mkDirs(attrs*]]
    */
  def mkDirs: Try[Self] = mkDirs()

  /**
    * relative depth to a given `FSEntry`
    *
    * @example
    * {{{
    * import sclib.io._
    * scala> for {
    *      |  a <- dir("/tmp")
    *      |  b <- dir("/tmp/a/b")
    *      | } yield a.depth(b)
    * res0: scala.util.Try[Int] = Success(2)
    * }}}
    */
  def depth(other: FSEntryImpl): Int = {
    def elements(x: FSEntryImpl): Int = x.absNormalizedPath.getNameCount
    Math.abs(elements(this) - elements(other))
  }

  /**
    * check if the file exists
    *
    */
  def exists: Boolean = Files.exists(path)

  /**
    * check if the file is a directory
    */
  def isDirectory: Boolean = Files.isDirectory(path)

  /**
    * check if the file is a regular file
    */
  def isRegularFile: Boolean = Files.isRegularFile(path)

  /**
    * file modification time in milliseconds, since the epoch (1970-01-01T00:00:00Z)
    */
  def mtime: Try[Long] = Try(Files.getLastModifiedTime(path).toMillis)

  /**
    * set the modification time
    *
    * @param millis file modification time in milliseconds, since the epoch (1970-01-01T00:00:00Z)
    */
  def mtime(millis: Long): Try[Unit] = Try {
    Files.setLastModifiedTime(path, FileTime.fromMillis(millis))
  }

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
  def copy(target: Self, options: Seq[CopyOption] = Seq()): Try[Self] = Try {
    Files.copy(path, target.path, options:_*)
    this
  }

  /**
    * move the file
    *
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html]]
    */
  def move(target: Self, options: Seq[CopyOption] = Seq()): Try[Self] = Try {
    Files.move(path, target.path, options:_*)
    this
  }

  /**
    * set file permission mode
    *
    * @param perms: sequence of [[http://http://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/PosixFilePermission.html]]
    */
  def chmod(perms: Seq[PosixFilePermission]): Try[Self] = Try {
    Files.setPosixFilePermissions(path, setAsJavaSet(perms.toSet))
    this
  }

  /**
    * set file permission mode
    *
    * @example
    * {{{
    * chmod(700) -> chmod(Seq(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))
    * chmod(644) -> chmod(Seq(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ))
    * }}}
    * @param n: unix like file permission notation
    */
  def chmod(n: Int): Try[Self] = FSPerm.calc(n).flatMap(chmod)

  /**
    * set file permission mode
    *
    * @example
    * {{{
    * chmod("a=r,u+w") -> chmod(Seq(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ))
    * chmod("a+x")     -> current permissions + Seq(OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE)
    * }}}
    * @param mode: list of unix like symbolic permissions notation
    */
  def chmod(mode: String): Try[Self] =
    for {
      actualPerms <- lsmod
      newPerms    <- FSPerm.mod(actualPerms, mode)
      res         <- chmod(newPerms)
    } yield res

  /**
    * get file permission mode
    *
    * @return
    */
  def lsmod: Try[Seq[PosixFilePermission]] = Try {
    Files.getPosixFilePermissions(path).toList
  }
}
object FSEntry {

  type FSEntryImpl = FSEntry[A] forSome { type A <: FSEntry[A] }
}