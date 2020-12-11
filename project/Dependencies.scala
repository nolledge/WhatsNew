import sbt._

object Dependencies {

  object Version {
    lazy val refined = "0.9.19"
    lazy val scalaTest = "3.2.2"
    lazy val bot4s = "4.4.0-RC2"
    lazy val sttp = "1.7.2"
    lazy val cats = "2.3.0"
    lazy val newType = "0.4.4"
    lazy val redis4Cats = "0.10.3"
    lazy val log4Cats = "1.1.1"
    lazy val logback = "1.2.1"
    lazy val pureCfg = "0.14.0"
    lazy val scalaScraper = "2.2.0"
    lazy val fs2 = "2.4.6"
  }

  lazy val scalaTest =
    "org.scalatest" %% "scalatest" % Version.scalaTest % "test,it"

  lazy val refined = Seq(
    "eu.timepit" %% "refined" % Version.refined,
    "eu.timepit" %% "refined-cats" % Version.refined
  )

  lazy val bot4s = "com.bot4s" %% "telegram-core" % Version.bot4s

  lazy val sttpCore =
    "com.softwaremill.sttp" %% "core" % Version.sttp
  lazy val sttpBackendCats =
    "com.softwaremill.sttp" %% "async-http-client-backend-cats" % Version.sttp

  lazy val scalaScraper =
    "net.ruippeixotog" %% "scala-scraper" % Version.scalaScraper

  lazy val cats = Seq(
    "org.typelevel" %% "cats-effect" % Version.cats,
    "org.typelevel" %% "cats-core" % Version.cats
  )
  lazy val log4Cats = Seq(
    "io.chrisdavenport" %% "log4cats-core" % Version.log4Cats,
    "io.chrisdavenport" %% "log4cats-slf4j" % Version.log4Cats
  )

  lazy val redis4Cats =
    "dev.profunktor" %% "redis4cats-effects" % Version.redis4Cats

  lazy val redis4CatsStreams =
    "dev.profunktor" %% "redis4cats-streams" % Version.redis4Cats

  lazy val newType = "io.estatico" %% "newtype" % Version.newType

  lazy val logback = "ch.qos.logback" % "logback-classic" % Version.logback

  lazy val pureCfg = Seq(
    "com.github.pureconfig" %% "pureconfig" % Version.pureCfg,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % Version.pureCfg
  )

  lazy val fs2 = "co.fs2" %% "fs2-core" % Version.fs2

  lazy val coreDependencies =
    refined ++ cats ++ log4Cats ++ Seq(
      newType,
      redis4Cats,
      logback,
      scalaTest
    )

  lazy val apiDependencies =
    cats ++ pureCfg ++ Seq(
      bot4s,
      sttpBackendCats,
      newType
    )

  lazy val workerDependencies =
    cats ++ pureCfg ++ Seq(
      bot4s,
      sttpCore,
      sttpBackendCats,
      newType,
      scalaScraper,
      fs2
    )
}
