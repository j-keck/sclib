package sclib.ops

object int extends int

/**
  * `Int` extensions
  *
  * ''check the member documentation for examples''
  */
trait int {

  implicit class IntOps(i: Int) {
    def B: Long = i.toLong
    def KB: Long = B * 1024
    def MB: Long = KB * 1024
    def GB: Long = MB * 1024
  }
}
