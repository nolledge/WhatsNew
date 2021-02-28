import Dependencies._

ThisBuild / scalaVersion     := "2.12.12"
ThisBuild / version          := "0.3.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  scalaVersion := "2.12.12",
  Global / onChangedBuildSource := ReloadOnSourceChanges,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "utf-8",
    "-explaintypes",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfuture",
    "-Xlint:by-name-right-associative",
    "-Xlint:constant",
    "-Xlint:delayedinit-select",
    "-Xlint:inaccessible",
    "-Xlint:infer-any",
    "-Xlint:missing-interpolator",
    "-Xlint:nullary-override",
    "-Xlint:nullary-unit",
    "-Xlint:option-implicit",
    "-Xlint:package-object-classes",
    "-Xlint:poly-implicit-overload", 
    "-Xlint:private-shadow",
    "-Xlint:stars-align",
    "-Xlint:type-parameter-shadow",
    "-Xlint:unsound-match",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible", 
    "-Ywarn-infer-any", 
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit", 
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports", 
    "-Ywarn-unused:locals", 
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-value-discard"
  )
)

lazy val debianSettings = Seq(
  version in Debian := "1.0.0",
  packageSummary := "Whats New API and worker",
  packageDescription := "Telegram controlled webcrawler",
)

def baseProject(name: String): Project =
  Project(name, file(name))
    .settings(debianSettings ++ commonSettings: _*)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)

lazy val whatsNew = (project in file("."))
  .aggregate(core, api, worker)

lazy val core = baseProject("core")
  .settings(
    libraryDependencies ++= Dependencies.coreDependencies
  )


lazy val api = baseProject("api")
  .dependsOn(core % "compile->compile;test->test;it->it")
  .settings(
    libraryDependencies ++= Dependencies.apiDependencies
    // Compile / mainClass := Some("com.dreamlines.dragonfly.api.Starter")
  )
  .enablePlugins(DebianPlugin)
  .enablePlugins(JavaAppPackaging)

lazy val worker = baseProject("worker")
  .dependsOn(core % "compile->compile;test->test;it->it")
  .settings(
    libraryDependencies ++= Dependencies.workerDependencies
    // Compile / mainClass := Some("com.dreamlines.dragonfly.api.Starter")
  )
  .enablePlugins(DebianPlugin)
  .enablePlugins(JavaAppPackaging)

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
