package whatsnew

import cats.effect.{Async, ContextShift}
import cats.implicits._
import com.bot4s.telegram.cats.TelegramBot
import com.bot4s.telegram.cats.Polling
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import CoreEntities._
import scala.util.Try

import eu.timepit.refined.auto._

class WhatsNewBot[F[_]: Async: ContextShift](
    token: String,
    searches: SearchesAlg[F]
) extends TelegramBot(token, AsyncHttpClientCatsBackend())
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
        Try(fArg.toInt)
          .fold(
            _ => reply(s"parameter is not a valid number").void,
            i =>
              for {
                items <- searches.getByChat(msg.source)
                delUrl = items.get(i.toLong).map(_.url)
                _ <- delUrl.fold(reply("index does not exist"))(url =>
                  searches
                    .deleteUrl(msg.source, url)
                    .flatMap(_ => reply(s"deleted search job $i"))
                )
              } yield ()
          )

      case _ =>
        reply("Invalid argument. Usage: /add http://host/searchquery=xyz").void
    }
  }
}
