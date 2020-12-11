package whatsnew

import cats.effect._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object WorkerProgram extends IOApp {

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- ConfigSource.default.loadF[IO, Config]
      backend = AsyncHttpClientCatsBackend[IO]()
      redisSearches = new RedisSearchesInt[IO](config.redis.url)
        with SearchesAlg[IO]
      itemIds = new RedisItemIdInt[IO](config.redis.url)
      itemExtractor = new EbayKleinanzeigenExt(backend)
      responder = new TelegramResponder[IO](config.bot.token)
      workerJob = new WorkerJob[IO](
        redisSearches,
        itemIds,
        itemExtractor,
        responder,
        config.job.requestInterval
      )
      _ <- workerJob.runJob()
      _ = responder.shutdown()
    } yield ExitCode.Success

}
