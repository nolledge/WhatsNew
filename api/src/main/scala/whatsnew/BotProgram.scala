package whatsnew

import cats.effect._

import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

object BotProgram extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- ConfigSource.default.loadF[IO, Config]
      redisSearches = new RedisSearchesInt[IO](config.redis.url)
        with SearchesAlg[IO]
      redisNotes = new RedisNotesInt[IO](config.redis.url)
        backend <- AsyncHttpClientCatsBackend[IO]()
      bot = new WhatsNewBot[IO](config.bot.token, redisSearches, redisNotes, backend)
      _ <- bot.run()
    } yield ExitCode.Success

}
