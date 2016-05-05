import sbt._
import Keys._
import sbt.complete.Parser
import sbtrelease.ReleasePlugin.autoImport.ReleaseStep
import sbtrelease.Vcs

/**
  * quick hack to update the version in 'README.md'
  *
  * this will be called from 'sbt-release' at release time.
  *
  *   - read the sbt snippet from 'sbt-snippet.xml'
  *   - replace the snippet in 'src/main/tut/README.md'
  *   - execute tut
  *   - add 'README.md' and 'src/main/tut/README.md' to the git index
  *     'sbt-release' commit it later
  */
object ReplaceSbtSnippet extends AutoPlugin {


  val action = (st: State) =>
    {
      val extracted = Project.extract(st)
      val log = extracted.get(Keys.sLog)

      // actual version
      val version = extracted.get(Keys.version)

      // project base dir
      val baseDir = extracted.get(Keys.baseDirectory)

      // readme path
      val readmePath = baseDir.toString + "/src/main/tut/README.md"

      // readme content
      val readme = IO.read(file(readmePath))

      // snippet with updated version
      val snippet = {
        val path    = baseDir.toString + "/sbt-snippet.txt"
        val nl = System.getProperty("file.separator")
        val snippet = IO.readLines(file(path)).filterNot(_.startsWith("#")).mkString(nl)
        snippet.replaceAll("""\$VERSION\$""", version)
      }

      // HACK!
      log.info(s"update version in ${readmePath} to: ${version}")
      val updated = readme.replaceFirst("(?s)<sbt-snippet>.*</sbt-snippet>", snippet)

      // write readme
      IO.write(file(readmePath), updated)

      // execute tut
      tut(st)

      // add the updated README.md in the git index.
      // 'sbt-release' commit it later.
      Vcs.detect(baseDir).foreach(_.add(readmePath, baseDir.toString + "/README.md") !! st.log)

      st
  }

  val replaceSbtSnippet = ReleaseStep(action = action)

  private def tut: State => State = { st =>
    Parser.parse("tut", st.combinedParser) match {
      case Right(cmd) => cmd()
      case Left(msg) => throw sys.error("tut not found!")
    }
  }
}
