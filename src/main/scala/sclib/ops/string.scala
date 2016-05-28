package sclib.ops

import java.text.SimpleDateFormat
import java.util.Date

import scala.util.Try
import sclib.ops.`try`._
import sclib.ops.either._

object string extends string

/**
  * `String` extensions
  *
  * ''check the member documentation for examples''
  */
trait string {

  implicit class StringOps(s: String) {

    def parseInt[F[_]](implicit pfs: ParsedFromString[F, Int]): F[Int] = pfs.fromTry(Try(s.toInt))

    def parseLong[F[_]](implicit pfs: ParsedFromString[F, Long]): F[Long] = pfs.fromTry(Try(s.toLong))

    def parseDouble[F[_]](implicit pfs: ParsedFromString[F, Double]): F[Double] = pfs.fromTry(Try(s.toDouble))

    def parseChar[F[_]](implicit pfs: ParsedFromString[F, Char]): F[Char] =
      pfs.fromTry(
          s.toList match {
        case List(c) => c.success
        case Nil     => "empty string doesn't contain any char".failure
        case _       => s"'${s}' contains more than a char".failure
      })

    def parseBoolean[F[_]](implicit pfs: ParsedFromString[F, Boolean]): F[Boolean] = pfs.fromTry(Try(s.toBoolean))

    def parseDate[F[_]](implicit pfs: ParsedFromString[F, Date], sdf: SimpleDateFormat): F[Date] =
      pfs.fromTry(Try(sdf.parse(s)))

    def parseDate[F[_]](pattern: String)(implicit pfs: ParsedFromString[F, Date]): F[Date] =
      pfs.fromTry(
          Try {
        val sdf = new SimpleDateFormat(pattern)
        sdf.parse(s)
      })
  }

  /**
    * Type-Class for the result of `StringOps.parseXX`
    */
  trait ParsedFromString[F[_], A] {
    def fromTry(t: Try[A]): F[A]
  }

  object ParsedFromString {
    implicit def tryFromTry[A] = new ParsedFromString[Try, A] {
      override def fromTry(t: Try[A]): Try[A] = t
    }

    implicit def optionFromTry[A] = new ParsedFromString[Option, A] {
      override def fromTry(t: Try[A]): Option[A] = t.toOption
    }
    implicit def eitherFromTry[A] = new ParsedFromString[Either[Throwable, ?], A] {
      override def fromTry(t: Try[A]): Either[Throwable, A] = t.toEither
    }

    implicit def eitherFromTryWithLepfsString[A] = new ParsedFromString[Either[String, ?], A] {
      override def fromTry(t: Try[A]): Either[String, A] =
        t.toEither.mapO(e => e.getClass.getName + ": " + e.getMessage)
    }
  }
}
