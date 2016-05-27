package sclib.io.fs

import java.nio.file.{CopyOption, Files, Path, Paths}
import java.nio.file.attribute.{FileAttribute, FileTime, PosixFilePermission, PosixFilePermissions}
import java.nio.file.attribute.PosixFilePermission._

import scala.util.Try
import scala.collection.JavaConverters._
import sclib.ops.`try`._

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

  protected[fs] def withPath(p: Path): Self

  /**
    * create the filesystem entry with default permissions
    *
    * ''the path hierarchy must exist - use [[FSEntry.mkDirs(attrs:java\.nio\.file\.attribute\.FileAttribute[_]*)*]] to create the hierarchy at first.''
    */
  def create: Try[Self] = this match {
    case _: FSFile =>
      create(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ)
    case _: FSDir =>
      create(OWNER_READ,
             OWNER_WRITE,
             OWNER_EXECUTE,
             GROUP_READ,
             GROUP_EXECUTE,
             OTHERS_READ,
             OTHERS_EXECUTE)
  }

  /**
    * create the filesystem entry with the given permissions
    *
    * ''the path hierarchy must exist - use [[FSEntry.mkDirs(attrs:java\.nio\.file\.attribute\.FileAttribute[_]*)*]] to create the hierarchy at first.''
    *
    */
  def create(perms: PosixFilePermission*): Try[Self] = Try {
    this match {
      case _: FSFile =>
        Files.createFile(path, PosixFilePermissions.asFileAttribute(perms.toSet.asJava))
      case _: FSDir =>
        Files.createDirectory(path, PosixFilePermissions.asFileAttribute(perms.toSet.asJava))
    }
    this
  }

  /**
    * create the filesystem entry with the given permissions
    *
    * ''the path hierarchy must exist - use [[FSEntry.mkDirs(attrs:java\.nio\.file\.attribute\.FileAttribute[_]*)* FSEntry.mkDirs(attrs: FileAttribute*)]] to create the hierarchy at first.''
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
    * @param n unix like numeric mode
    */
  def create(n: Int): Try[Self] = FSPerm.calc(n).flatMap(create(_: _*))

  /**
    * create the filesystem entry with the given permissions
    *
    * ''the path hierarchy must exist - use [[FSEntry.mkDirs(attrs:java\.nio\.file\.attribute\.FileAttribute[_]*)* FSEntry.mkDirs(attrs: FileAttribute*)]] to create the hierarchy at first.''
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
    *
    * @param mode unix like symbolic mode
    */
  def create(mode: String): Try[Self] =
    for {
      perms <- FSPerm.mod(Seq(), mode)
      res   <- create(perms: _*)
    } yield res

  /**
    * create a new temporary filesystem entry.
    * the actual name is used for the prefix.
    *
    * if no absolute path is given, the entry is created in the
    * temp directory (determined by: System.getProperty("java.io.tmpdir")).
    *
    * @example
    * {{{
    * scala> for {
    *      |   fh <- dir("a-temp-dir")
    *      |   // override the original fh
    *      |   fh <- fh.createTemp
    *      |   _ <- fh.delete
    *      |   // fh.name is something like 'a-temp-dir7526811586167481701'
    *      | } yield fh.name.matches("a-temp-dir\\d+")
    * res0: scala.util.Try[Boolean] = Success(true)
    *
    *
    * scala> for {
    *      |   fh <- file("a-temp-file")
    *      |   // override the original fh
    *      |   fh <- fh.createTemp
    *      |   _ <- fh.delete
    *      |   // fh.name is something like 'a-temp-file9014711075515420555'
    *      | } yield fh.name.matches("a-temp-file\\d+")
    * res0: scala.util.Try[Boolean] = Success(true)
    *
    * scala> for {
    *      |   fh <- file("a-temp-file.txt")
    *      |   // override the original fh
    *      |   fh <- fh.createTemp
    *      |   _ <- fh.delete
    *      |   // fh.name is something like 'a-temp-file2068553337840465580.txt'
    *      | } yield fh.name.matches("a-temp-file\\d+\\.txt")
    * res1: scala.util.Try[Boolean] = Success(true)
    * }}}
    */
  def createTemp: Try[Self] = Try {
    this match {
      case _: FSDir =>
        withPath(Option(path.getParent).fold(Files.createTempDirectory(name))(Files.createTempDirectory(_, name)))
      case _: FSFile =>
        val (prefix, suffix) = name.reverse.split("\\.", 2) match {
          case Array(s, p) => (p.reverse, "." + s.reverse)
          case Array(p)    => (p.reverse, "")
        }
        withPath(
            Option(path.getParent).fold(Files.createTempFile(prefix, suffix))(Files.createTempFile(_, prefix, suffix)))
    }
  }

  /**
    * create the directories
    *
    * - when called on a `FSDir`, the directroy's path (inclusive parents) are created
    * - when called on a `FSFile` the parent hierarchy are created
    *
    * @param attrs [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/PosixFileAttributes.html]]
    */
  def mkDirs(attrs: FileAttribute[_]*): Try[Self] = Try {
    val p = this match {
      case _: FSDir  => path
      case _: FSFile =>
        // 'getParent' can return 'null'. but because it's a file, there it has always a parent.
        absNormalizedPath.getParent
    }
    Files.createDirectories(p, attrs: _*)
    this
  }

  /**
    * @see [[FSEntry.mkDirs(attrs:java\.nio\.file\.attribute\.FileAttribute[_]*)*]]
    */
  def mkDirs: Try[Self] = mkDirs()

  /**
    * relative depth to a given `FSEntry`
    *
    * @example
    * {{{
    * scala> for {
    *      |  a <- dir("sclib-example")
    *      |  b <- dir(a, "x/y/z/..")
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
    * copy the entry (file -> file, dir -> dir)
    *
    * @example
    * {{{
    * scala> for {
    *      |   wd <- dir("sclib-example")
    *      |   wd <- wd.createTemp
    *      |   src <- file(wd, "a-file")
    *      |   _ <- src.write("content")
    *      |   dst <- file(wd, "b-file")
    *      |   _ <- src.copy(dst)
    *      |   c <- dst.slurp
    *      |   _ <- wd.deleteR
    *      | } yield c
    * res0: scala.util.Try[String] = Success(content)
    * }}}
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html]]
    *
    */
  def copy(target: Self, options: CopyOption*): Try[Self] = Try {
    withPath(Files.copy(path, target.absNormalizedPath, options: _*))
  }

  /**
    * copy the entry to the given dir
    *
    * if the target dir doesn't exists, it's being created
    *
    * use [[FSEntry.copyToR(target*]] for a recursive copy operation
    */
  def copyTo(target: FSDir, options: CopyOption*): Try[Self] =
    for {
      // create the target directory if it doesn't exist
      _ <- target.mkDirs
      t = Paths.get(target.absNormalizedPath.toString + System.getProperty("file.separator") + this.name)
      s <- Try(withPath(Files.copy(path, t, options: _*)))
    } yield s

  /**
    * recursively copy the entry to the given dir
    *
    * if the target dir doesn't exists, it's being created
    */
  def copyToR(target: FSDir, options: CopyOption*): Try[Self] = {
    this match {
      case d: FSDir =>
        for {
          // copy the directory
          t <- d.copyTo(target, options: _*)
          // copy recursive
          _ <- d.flatMap { e: FSEntryImpl =>
                dir(t.path.toString + System.getProperty("file.separator") + e.name).flatMap[FSEntryImpl] {
                  e.copyToR(_, options: _*)
                }
              }.toList.sequence
        } yield withPath(target.path)
      case _: FSFile =>
        copyTo(target, options: _*)
    }
  }

  /**
    * move the entry
    *
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html]]
    */
  def move(target: Self, options: CopyOption*): Try[Self] = Try {
    withPath(Files.move(path, target.path, options: _*))
  }

  /**
    * move the entry to the given directory, and keep the name
    *
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html]]
    */
  def moveTo(dir: FSDir, options: CopyOption*): Try[Self] =
    for {
      // create the target directory if it doesn't exist
      _ <- dir.mkDirs
      s <- Try(withPath(Files.move(path, Paths.get(dir.path.toString, name), options: _*)))
    } yield s

  /**
    * rename the entry
    *
    * @example
    * {{{
    * scala> for {
    *      |   wd <- dir("sclib-example")
    *      |   wd <- wd.createTemp
    *      |   src <- file(wd, "a-file")
    *      |   _ <- src.create
    *      |   dst <- src.renameTo("b-file")
    *      |   res = (src.exists, dst.exists)
    *      |   _ <- wd.deleteR
    *      | } yield res
    * res0: scala.util.Try[(Boolean, Boolean)] = Success((false,true))
    * }}}
    * @param options [[http://docs.oracle.com/javase/8/docs/api/java/nio/file/StandardCopyOption.html]]
    *
    */
  def renameTo(newName: String, options: CopyOption*): Try[Self] = Try {
    val parent = Option(path.getParent).fold("")(_.toString)
    withPath(Files.move(path, Paths.get(parent, newName)))
  }

  /**
    * set entry permission mode
    *
    * @example
    * {{{
    * scala> import java.nio.file.attribute.PosixFilePermission._
    * scala> for {
    *      |   wd <- dir("sclib-example")
    *      |   wd <- wd.createTemp
    *      |   fh <- file(wd, "a-file").flatMap(_.create)
    *      |   _ <- fh.chmod(OWNER_READ)
    *      |   mod <- fh.lsmod
    *      |   _ <- wd.deleteR()
    *      | } yield mod
    * res0: scala.util.Try[Seq[java.nio.file.attribute.PosixFilePermission]] = Success(List(OWNER_READ))
    * }}}
    * @param perms : sequence of [[http://http://docs.oracle.com/javase/8/docs/api/java/nio/file/attribute/PosixFilePermission.html]]
    */
  def chmod(perms: PosixFilePermission*): Try[Self] = Try {
    Files.setPosixFilePermissions(path, perms.toSet.asJava)
    this
  }

  /**
    * set entry permission mode recursive
    *
    * ''fail on any error''
    *
    * @see [[FSEntry.chmod(perms:java\.nio\.file\.attribute\.PosixFilePermission*)*]]
    */
  def chmodR(perms: PosixFilePermission*): Try[Self] = {
    this match {
      case d: FSDir =>
        Try {
          // try to apply the given permissions recursive to each entry - fail on any error.
          FSIterator(d, includeStartDir = true).foreach(_.fold(throw _)(_.chmod(perms: _*).get))
          this
        }
      case _: FSFile => chmod(perms: _*)
    }
  }

  /**
    * set entry permission mode
    *
    * @example
    * {{{
    * chmod(700) -> chmod(Seq(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))
    * chmod(644) -> chmod(Seq(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ))
    * }}}
    * @param n : unix like file permission notation
    */
  def chmod(n: Int): Try[Self] = FSPerm.calc(n).flatMap(chmod(_: _*))

  /**
    * set entry permission mode recursive
    *
    * ''fail on any error''
    *
    * @see [[FSEntry.chmod(n:Int)*]]
    */
  def chmodR(n: Int): Try[Self] =
    for {
      perm <- FSPerm.calc(n)
      _    <- chmodR(perm: _*)
    } yield this

  /**
    * set entry permission mode
    *
    * @example
    * {{{
    * chmod("a=r,u+w") -> chmod(Seq(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ))
    * chmod("a+x")     -> current permissions + Seq(OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE)
    * }}}
    * @param mode : list of unix like symbolic permissions notation
    */
  def chmod(mode: String): Try[Self] =
    for {
      actualPerms <- lsmod
      newPerms    <- FSPerm.mod(actualPerms, mode)
      res         <- chmod(newPerms: _*)
    } yield res

  /**
    * set entry permission mode recursive
    *
    * ''fail on any error''
    *
    * @see [[FSEntry.chmod(mode:String)*]]
    */
  def chmodR(mode: String): Try[Self] = this match {
    case d: FSDir =>
      Try {
        // try to apply the given permissions recursive to each entry - fail on any error.
        FSIterator(d, includeStartDir = true).foreach(_.fold(throw _)(_.chmod(mode).get))
        this
      }
    case _: FSFile => chmod(mode)
  }

  /**
    * get entry permission mode
    *
    * @return
    */
  def lsmod: Try[Seq[PosixFilePermission]] = Try {
    Files.getPosixFilePermissions(path).asScala.toList
  }
}
