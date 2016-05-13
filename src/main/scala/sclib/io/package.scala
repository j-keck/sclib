package sclib

package object io {

  type AutoCloseableResource[A] = Traversable[A]

  /**
    * simple version of java's 'try-with-resource'
    *
    * exceptions aren't intercepted, only the resource will always be closed.
    *
    * @example {{{
    * import sclib.io.autoClose
    * import java.nio.file.Paths
    * import java.nio.file.StandardOpenOption.{CREATE, WRITE}
    * import java.nio.channels.FileChannel
    * for {
    *   in <- autoClose(FileChannel.open(Paths.get("/tmp/input")))
    *   out <- autoClose(FileChannel.open(Paths.get("/tmp/output"), CREATE, WRITE))
    * } in.transferTo(0, Long.MaxValue, out)
    * // `in` and `out` are closed here
    * }}}
    *
    */
  def autoClose[A <: AutoCloseable](a: A): AutoCloseableResource[A] = new Traversable[A] {
    override def foreach[B](f: (A) => B): Unit = {
      try {
        f(a)
      } finally {
        a.close()
      }
    }
  }
}
