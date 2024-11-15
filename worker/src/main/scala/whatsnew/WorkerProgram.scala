package whatsnew

import cats.effect._

import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

object WorkerProgram extends IOApp {

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  def run(args: List[String]): IO[ExitCode] =
    AsyncHttpClientCatsBackend.resource[IO]().use { backend =>
      for {
        config <- ConfigSource.default.loadF[IO, Config]
        redisSearches =
          new RedisSearchesInt[IO](config.redis.url) with SearchesAlg[IO]
        itemIds = new RedisItemIdInt[IO](config.redis.url)
        itemExtractor = new EbayKleinanzeigenExt(backend)
        responder = new TelegramResponder[IO](config.bot.token, backend)
        workerJob = new WorkerJob[IO](
          redisSearches,
          itemIds,
          itemExtractor,
          responder,
          config.job.requestInterval
        )
        _ <- workerJob.runJob()
      } yield ExitCode.Success
    }

}
