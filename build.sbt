lazy val buildSettings = Seq(
  name := "sclib",
  organization := "net.jkeck",

  version := "0.1",
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
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked"
  ),

  scalacOptions in console in Compile -= "-Xfatal-warnings",
  scalacOptions in console in Test    -= "-Xfatal-warnings",


  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
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
