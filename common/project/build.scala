import sbt._
import Keys._

object BuildSettings extends Build {
  override lazy val settings = super.settings ++ Seq(
    organization := "berkeley",
    version      := "1.2",
    scalaVersion := "2.12.4",
    parallelExecution in Global := false,
    traceLevel   := 15,
    scalacOptions ++= Seq("-deprecation","-unchecked","-Xsource:2.11")
  )
  lazy val zynq = (project in file("."))
}
