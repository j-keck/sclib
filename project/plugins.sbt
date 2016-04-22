logLevel := Level.Warn

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.4.2")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.8")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.0.0")

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.4")

addSbtPlugin("com.geirsson" %% "sbt-scalafmt" % "0.2.1")

addSbtPlugin("com.github.tkawachi" % "sbt-doctest" % "0.4.0")