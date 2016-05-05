import ReleaseTransformations._
import ReplaceSbtSnippet._

lazy val buildSettings = Seq(
  name := "sclib",
  organization := "net.jkeck",
 
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", scalaVersion.value)
)

lazy val commonSettings = Seq(
  resolvers += Resolver.bintrayRepo("j-keck", "sclib"),
  homepage := Some(url("https://github.com/j-keck/sclib")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(url("https://github.com/j-keck/sclib"), "scm:git:git@github.com:j-keck/sclib.git")),

  scalacOptions := Seq(
    "-feature",
    "-language:higherKinds",
    "-language:existentials",
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked"
  ),

  scalacOptions in console in Compile -= "-Xfatal-warnings",
  scalacOptions in console in Test    -= "-Xfatal-warnings",


  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  ),

  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    releaseStepCommand("ghpagesPushSite"),
    setReleaseVersion,
    replaceSbtSnippet,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)



lazy val root = project
  .in(file("."))
  .aggregate(sclibJS, sclibJVM)
  .settings(buildSettings:_*)
  .settings(commonSettings:_*)
  .settings(tutSettings)
  .settings(
    publish := (),
    publishLocal := (),
    publishArtifact := false,

    tutTargetDirectory := baseDirectory.value
  )


lazy val sclib = crossProject.crossType(CrossType.Pure)
  .in(file("."))
  .settings(buildSettings:_*)
  .settings(commonSettings:_*)


lazy val sclibJVM = sclib.jvm
lazy val sclibJS = sclib.js

enablePlugins(ScalaJSPlugin)

enablePlugins(SiteScaladocPlugin)

ghpages.settings

git.remoteRepo := "git@github.com:j-keck/sclib.git"
