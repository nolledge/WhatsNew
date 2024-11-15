import sbt._

object Dependencies {

  object Version {
    lazy val refined = "0.11.2"
    lazy val scalaTest = "3.2.2"
    lazy val bot4s = "5.8.3"
    lazy val sttp = "3.10.1"
    lazy val catsEffect = "3.5.4"
    lazy val redis4Cats = "1.7.1"
    lazy val log4Cats = "2.7.0"
    lazy val logback = "1.5.12"
    lazy val pureCfg = "0.17.7"
    lazy val scalaScraper = "2.2.1"
    lazy val fs2 = "3.11.0"
    lazy val catsEffectTesting = "1.5.0"
  }

  lazy val scalaTest =
    "org.scalatest" %% "scalatest" % Version.scalaTest % "test,it"

  lazy val catsEffectTesting =
    "org.typelevel" %% "cats-effect-testing-scalatest" % Version.catsEffectTesting % "test,it"

  lazy val refined = Seq(
    "eu.timepit" %% "refined" % Version.refined,
    "eu.timepit" %% "refined-cats" % Version.refined
  )

  lazy val bot4s = "com.bot4s" %% "telegram-core" % Version.bot4s

  lazy val sttpCore =
    "com.softwaremill.sttp.client3" %% "core" % Version.sttp
  lazy val sttpBackendCats =
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % Version.sttp

  lazy val scalaScraper =
    "net.ruippeixotog" %% "scala-scraper" % Version.scalaScraper

  lazy val cats = Seq(
    "org.typelevel" %% "cats-effect" % Version.catsEffect
  )
  lazy val log4Cats = Seq(
    "org.typelevel" %% "log4cats-core" % Version.log4Cats,
    "org.typelevel" %% "log4cats-slf4j" % Version.log4Cats
  )

  lazy val redis4Cats =
    "dev.profunktor" %% "redis4cats-effects" % Version.redis4Cats

  lazy val redis4CatsStreams =
    "dev.profunktor" %% "redis4cats-streams" % Version.redis4Cats

  lazy val logback = "ch.qos.logback" % "logback-classic" % Version.logback

  lazy val pureCfg = Seq(
    "com.github.pureconfig" %% "pureconfig" % Version.pureCfg,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % Version.pureCfg
  )

  lazy val fs2 = "co.fs2" %% "fs2-core" % Version.fs2

  lazy val coreDependencies =
    refined ++ cats ++ log4Cats ++ Seq(
      redis4Cats,
      logback,
      scalaTest,
      catsEffectTesting
    )

  lazy val apiDependencies =
    cats ++ pureCfg ++ Seq(
      bot4s,
      sttpBackendCats
    )

  lazy val workerDependencies =
    cats ++ pureCfg ++ Seq(
      bot4s,
      sttpCore,
      sttpBackendCats,
      scalaScraper,
      fs2
    )
}
