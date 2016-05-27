package sclib.io.fs

import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission._

import sclib.ops.all._

import scala.collection.SortedMap
import scala.util.{Either, Try}

/**
  * utility to calculate `PosixFilePermission`s from unix-style notation:
  *
  * <pre>
  *   - FSPerm.calc(644)      -> Seq(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ)
  *   - FSPerm.mod("a=r,u+w") -> Seq(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ)
  * </pre>
  */
object FSPerm {
  private val posixFilePermissions = SortedMap(
      400 -> OWNER_READ,
      200 -> OWNER_WRITE,
      100 -> OWNER_EXECUTE,
      40  -> GROUP_READ,
      20  -> GROUP_WRITE,
      10  -> GROUP_EXECUTE,
      4   -> OTHERS_READ,
      2   -> OTHERS_WRITE,
      1   -> OTHERS_EXECUTE
  )(Ordering[Int].reverse)

  /**
    * calculate from unix like file permission notation
    *
    * @example
    * {{{
    * scala> FSPerm.calc(700)
    * res0: scala.util.Try[Seq[java.nio.file.attribute.PosixFilePermission]] = Success(List(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE))
    * scala> FSPerm.calc(644)
    * res1: scala.util.Try[Seq[java.nio.file.attribute.PosixFilePermission]] = Success(List(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ))
    * }}}
    */
  def calc(n: Int): Try[Seq[PosixFilePermission]] = {
    val digits = n.toString.map(_.asDigit)
    if (digits.map(_ & 1111000).sum != 0) s"Invalid file mode: ${n}".failure
    else
      posixFilePermissions.find(_._1 <= n) match {
        case Some((m, p)) => calc(n - m).map(p +: _)
        case None         => Seq.empty[PosixFilePermission].success
      }
  }

  /**
    * calculate from list of unix like symbolic permissions notation
    *
    * {{{
    * scala> import scala.collection.SortedSet
    * scala> FSPerm.mod(Seq(), "a=r,u+wx").map(_.to[SortedSet])
    * res0: scala.util.Try[scala.collection.SortedSet[java.nio.file.attribute.PosixFilePermission]] = Success(TreeSet(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, OTHERS_READ))
    * scala> FSPerm.mod(Seq(), "u=rwx,g=rw,o=r").map(_.to[SortedSet])
    * res1:  scala.util.Try[scala.collection.SortedSet[java.nio.file.attribute.PosixFilePermission]] = Success(TreeSet(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_WRITE, OTHERS_READ))
    * }}}
    */
  def mod(act: Seq[PosixFilePermission], mode: String): Try[Seq[PosixFilePermission]] = {
    def go(modes: Seq[String]): Try[Seq[PosixFilePermission]] = modes match {
      case Nil => Nil.success
      case x :: xs =>
        for {
          posixFilePermissions <- ModeParser.parse(act, x)
          next                 <- go(xs)
        } yield posixFilePermissions ++: next
    }

    // FIXME: Seq.uniq
    go(mode.split(",").toList).map(_.toSet.toSeq)
  }

  private object ModeParser {

    sealed trait Who

    case object All extends Who

    case object User extends Who

    case object Group extends Who

    case object Others extends Who

    object Who {
      def apply(c: Char): Either[String, Who] = c match {
        case 'a' => All.right
        case 'u' => User.right
        case 'g' => Group.right
        case 'o' => Others.right
        case _   => s"invalid who: '${c}' symbol - expected: [a|u|g|o]".left
      }
    }

    sealed trait Op

    case object Add extends Op

    case object Del extends Op

    case object Set extends Op

    object Op {
      def apply(c: Char): Either[String, Op] = c match {
        case '+' => Add.right
        case '-' => Del.right
        case '=' => Set.right
        case _   => s"invalid op: '${c}' symbol - expected: [+|-|=]".left
      }
    }

    sealed trait Perm

    case object Read extends Perm

    case object Write extends Perm

    case object Exec extends Perm

    object Perm {
      def apply(c: Char): Either[String, Perm] = c match {
        case 'r' => Read.right
        case 'w' => Write.right
        case 'x' => Exec.right
        case _   => s"invalid perm: '${c}' symbol - expected: [r|w|x]".left
      }
    }

    def lookup(
        act: Seq[PosixFilePermission], who: Who, op: Char, perm: Perm): Either[String, Seq[PosixFilePermission]] = {
      val xs = (who, perm) match {
        case (All, Read)  => Seq(OWNER_READ, GROUP_READ, OTHERS_READ).right
        case (All, Write) => Seq(OWNER_WRITE, GROUP_WRITE, OTHERS_WRITE).right
        case (All, Exec) =>
          Seq(OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE).right
        case (User, Read)    => Seq(OWNER_READ).right
        case (User, Write)   => Seq(OWNER_WRITE).right
        case (User, Exec)    => Seq(OWNER_EXECUTE).right
        case (Group, Read)   => Seq(GROUP_READ).right
        case (Group, Write)  => Seq(GROUP_WRITE).right
        case (Group, Exec)   => Seq(GROUP_EXECUTE).right
        case (Others, Read)  => Seq(OTHERS_READ).right
        case (Others, Write) => Seq(OTHERS_WRITE).right
        case (Others, Exec)  => Seq(OTHERS_EXECUTE).right
        case _               => s"unexpected - who: ${who}, perm: ${perm} combination".left
      }

      xs.flatMap { ys =>
        op match {
          case '-' => act.diff(ys).right
          case '+' => (act ++ ys).right
          case '=' =>
            val keep: Seq[PosixFilePermission] = who match {
              case All => Seq()
              case User =>
                act.filterNot(Seq(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE).contains)
              case Group =>
                act.filterNot(Seq(GROUP_READ, GROUP_WRITE, GROUP_EXECUTE).contains)
              case Others =>
                act.filterNot(Seq(OTHERS_READ, OTHERS_WRITE, OTHERS_EXECUTE).contains)
            }
            (keep ++ ys).right
          case _ => s"unexpected op: ${op}".left
        }
      }
    }

    def lookupAll(act: Seq[PosixFilePermission],
                  whos: Seq[Who],
                  op: Char,
                  perms: Seq[Perm]): Either[String, Seq[PosixFilePermission]] =
      (for {
        w <- whos
        p <- perms
      } yield lookup(act, w, op, p)).sequence.map(_.flatten)

    def parse(act: Seq[PosixFilePermission], mode: String): Try[Seq[PosixFilePermission]] = {
      val idx = mode.indexWhere("+-=".contains(_))
      for {
        _ <- if (idx < 0) "operator [+|-|=] not found".left else ().right
        op = mode.charAt(idx)
        whos  <- mode.take(idx).map(Who.apply).sequence
        perms <- mode.drop(idx + 1).map(Perm.apply).sequence
        _     <- if (whos.isEmpty) "who ([a|u|g|o]+) not found".left else ().right
        _     <- if (perms.isEmpty) "perm ([r|w|x]+) not found".left else ().right
        res   <- lookupAll(act, whos, op, perms)
      } yield res
    }.toTry
  }
}
