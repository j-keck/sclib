package sclib.ops

import scala.util.Try
import sclib.ops.`try`._
import sclib.ops.either._

/**
  * `String` extensions
  *
  * ''check the member documentation for examples''
  */
object string {

  implicit class StringOps(s: String) {
    def toIntT: Try[Int] = Try(s.toInt)

    def toIntE: Either[String, Int] =
      toIntT.fold(t => s"'${s}' is not a Int: ${t.getLocalizedMessage}".left[Int])(_.right)

    def toLongT: Try[Long] = Try(s.toLong)

    def toLongE: Either[String, Long] =
      toLongT.fold(t => s"'${s}' is not a Long: ${t.getLocalizedMessage}".left[Long])(_.right)

    def toDoubleT: Try[Double] = Try(s.toDouble)

    def toDoubleE: Either[String, Double] =
      toDoubleT.fold(t => s"'${s}' is not a Double: ${t.getLocalizedMessage}".left[Double])(_.right)

    def toCharT: Try[Char] = toCharE.fold(_.failure, _.success)

    def toCharE: Either[String, Char] = s.toList match {
      case List(c) => c.right
      case Nil => "empty string doesn't contain any char".left
      case _ => s"'${s}' contains more than a char".left
    }

    def toBooleanT: Try[Boolean] = Try(s.toBoolean)

    def toBooleanE: Either[String, Boolean] =
      toBooleanT.fold(t => s"'${s}' is not a Boolean: ${t.getLocalizedMessage}".left[Boolean])(_.right)
  }

}
