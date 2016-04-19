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
    def toIntT: Try[Int] = Try(s.toInt)

    def toIntE: Either[String, Int] = toIntT.fold(_ => s"'${s}' is not a Int".left[Int])(_.right)

    def toLongT: Try[Long] = Try(s.toLong)

    def toLongE: Either[String, Long] = toLongT.fold(_ => s"'${s}' is not a Long".left[Long])(_.right)

    def toDoubleT: Try[Double] = Try(s.toDouble)

    def toDoubleE: Either[String, Double] = toDoubleT.fold(_ => s"'${s}' is not a Double".left[Double])(_.right)

    def toCharT: Try[Char] = toCharE.fold(_.failure, _.success)

    def toCharE: Either[String, Char] = s.toList match {
      case List(c) => c.right
      case Nil     => "empty string doesn't contain any char".left
      case _       => s"'${s}' contains more than a char".left
    }

    def toBooleanT: Try[Boolean] = Try(s.toBoolean)

    def toBooleanE: Either[String, Boolean] = toBooleanT.fold(_ => s"'${s}' is not a Boolean".left[Boolean])(_.right)

    def toDateT(implicit sdf: SimpleDateFormat): Try[Date] = Try {
      sdf.parse(s)
    }

    def toDateE(implicit sdf: SimpleDateFormat): Either[String, Date] =
      toDateT(sdf)
        .fold(_ => s"'${s}' is not a Date with the given format-pattern: '${sdf.toPattern}'".left[Date])(_.right)

    def toDateT(pattern: String): Try[Date] = Try {
      val sdf = new SimpleDateFormat(pattern)
      sdf.parse(s)
    }

    def toDateE(pattern: String): Either[String, Date] =
      toDateT(pattern)
        .fold(_ => s"'${s}' is not a Date with the given format-pattern: '${pattern}'".left[Date])(_.right)
  }
}
