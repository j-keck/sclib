package sclib.io

import java.net.URL
import java.nio.channels.{Channels, FileChannel, ReadableByteChannel}
import java.nio.file.StandardOpenOption._

import sclib.io.fs.FSFile
import sclib.ops.int._
import sclib.util.union._

import scala.annotation.tailrec
import scala.util.Try

/**
  * fetch a file from a given url
  *
  * to get a `java.net.URL` handle, use: [[net.url(s:String* url(s: String): Try[URL&#93;]]
  *
  * @example
  *
  * ====get a url handle====
  * {{{
  * scala: import sclib.io.net._
  * scala: url("http://example.com")
  * scala.util.Try[java.net.URL] = Success(http://example.com)
  * scala: url("example.com")
  * scala.util.Try[java.net.URL] = Failure(java.net.MalformedURLException: no protocol: example.com)
  * }}}
  *
  *
  * ====download a file====
  * {{{
  * scala: import sclib.io.net._
  * scala: import sclib.io.fs._
  * scala: url("http://example.com").flatMap(_.fetch(file("example.com")))
  * scala.util.Try[sclib.io.fs.FSFile] = Success(FSFile(example.com))
  * }}}
  */
object net {

  /**
    * save constructor for `java.net.URL`
    *
    */
  def url(s: String): Try[URL] = Try(new URL(s))

  /**
    * add the 'fetch' method to a `java.net.URL` instance
    */
  implicit class urlFetcher(url: URL) {

    /**
      * fetch a file from a given url
      *
      * simplified api:
      *
      * {{{fetch(url: URL, target: FSFile, connectTimeoutMS: Int = 0, readTimeoutMS: Int = 0): Try[FSFile]}}}
      * {{{fetch(url: URL, target: Try[FSFile], connectTimeoutMS: Int = 0, readTimeoutMS: Int = 0): Try[FSFile]}}}
      *
      *
      * the optional parameters `connectTimeoutMS` and `readTimeoutMS` are interpreted as milliseconds.
      * their default values are 0, with means a infinity timeout.
      *
      * @example {{{
      * import sclib.io.net._
      * import sclib.io.fs._
      * url("http://example.com").flatMap(_.fetch(file("example.com")))
      * }}}
      *
      * <pre>
      * implementation note:
      *   a union type is used here to use the function with either a 'FSFile' or a 'Try[FSFile]'.
      *   overloading the function - one for 'FSFile' and one for 'Try[FSFile]' is not usable because the
      *   use of default parameters.
      * </pre>
      *
      */
    def fetch[A : (FSFile Or Try[FSFile])#Check](
        target: A, connectTimeoutMS: Int = 0, readTimeoutMS: Int = 0): Try[FSFile] =
      target match {
        case f: FSFile =>
          Try {

            val con = url.openConnection()
            con.setConnectTimeout(connectTimeoutMS)
            con.setReadTimeout(readTimeoutMS)

            @tailrec
            def go(src: ReadableByteChannel, dst: FileChannel, pos: Long, bufSize: Long): Long = {
              val count = dst.transferFrom(src, pos, bufSize)
              if (count > 0) go(src, dst, pos + count, bufSize)
              else pos
            }

            for {
              src <- autoClose(Channels.newChannel(con.getInputStream))
              dst <- autoClose(FileChannel.open(f.path, CREATE, WRITE))
            } go(src, dst, 0, 1.MB)

            f
          }
        case x: Try[_] =>
          // this is save, because the function type force it to be a 'Try[FSFile]'
          x.asInstanceOf[Try[FSFile]].flatMap(fetch(_, connectTimeoutMS, readTimeoutMS))
      }
  }
}
