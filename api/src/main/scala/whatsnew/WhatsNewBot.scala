package whatsnew

import cats.effect.Async
import cats.implicits._

import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.cats.TelegramBot
import eu.timepit.refined.auto._
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import whatsnew.CoreEntities._
import whatsnew.Entities._

class WhatsNewBot[F[_]: Async](
    token: String,
    searches: SearchesAlg[F],
    backend: SttpBackend[F, Any]
) extends TelegramBot[F](token, backend)
    with Polling[F]
    with com.bot4s.telegram.api.declarative.Commands[F] {

  onCommand("/add") { implicit msg =>
    withArgs {
      case Seq(fArg) =>
        validateUrl(fArg)
          .fold(
            p => reply(s"parameter $p is not an url").void,
            url =>
              searches
                .create(msg.source, url)
                .flatMap(_ => reply("added search job").void)
          )

      case _ =>
        reply("Invalid argument. Usage: /add http://host/searchquery=xyz").void
    }
  }

  onCommand("/all") { implicit msg =>
    searches
      .getByChat(msg.source)
      .flatMap(
        _.map(job =>
          reply(s"URL: ${job.url}, Runs: ${job.runs}, Created: ${job.created}")
        ).sequence.void
      )
  }

  onCommand("/clean") { implicit msg =>
    searches
      .deleteAll(msg.source)
      .flatMap(_ => reply("deleted all items").void)
  }

  onCommand("/rm") { implicit msg =>
    withArgs {
      case Seq(fArg) =>
        validateUrl(fArg)
          .fold(
            _ => reply(s"parameter is not a valid url").void,
            url =>
              searches
                .deleteUrl(msg.source, url)
                .flatMap(_ => reply(s"deleted search job $url").void)
          )
      case _ =>
        reply("Invalid argument. Usage: /add http://host/searchquery=xyz").void
    }
  }
}
