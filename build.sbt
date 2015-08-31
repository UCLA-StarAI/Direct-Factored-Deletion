import de.heikoseeberger.sbtheader.HeaderPattern
import de.heikoseeberger.sbtheader.license.Apache2_0

lazy val sbtHeaderTest = project.in(file(".")).enablePlugins(AutomateHeaderPlugin)

organization := "edu.ucla.cs.reasoning"

name := "Deletion"

version := "1.0-SNAPSHOT"

crossPaths := false

autoScalaLibrary := false

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.2"

libraryDependencies += "commons-cli" % "commons-cli" % "1.3.1"


headers := Map(
  "java" -> Apache2_0("2015", "Guy Van den Broeck and Arthur Choi")
)
