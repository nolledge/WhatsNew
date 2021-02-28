package whatsnew

import cats.effect.{Async, ContextShift}
import cats.implicits._
import com.bot4s.telegram.cats.TelegramBot
import com.bot4s.telegram.cats.Polling
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import CoreEntities._
import Entities._

import eu.timepit.refined.auto._

class WhatsNewBot[F[_]: Async: ContextShift](
    token: String,
    searches: SearchesAlg[F],
    notes: NotesAlg[F]
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

  onCommand("/nadd") { implicit msg =>
    msg.text.map(_.split(" ").toList) match {
      case Some(_ :: name :: tail) =>
        notes
          .add(Note(msg.source, name, tail.mkString(" ")))
          .flatMap(_ => reply(s"added note $name").void)
      case _ =>
        reply("Invalid argument. Usage: /nadd <name> <message>").void

    }
  }

  onCommand("/nall") { implicit msg =>
    notes
      .allNames(msg.source)
      .flatMap { m =>
        val noteNames = m.mkString(",")
        reply(s"found note tempates: $noteNames").void
      }
  }

  onCommand("/n") { implicit msg =>
    withArgs {
      case Seq(name) =>
        notes
          .getByName(msg.source, name)
          .flatMap(
            _.fold(().pure[F])(t => reply(t.text).void)
          )
      case _ =>
        reply("Invalid argument. Usage: /n <templateName>").void
    }
  }

  onCommand("/nrm") { implicit msg =>
    withArgs {
      case Seq(name) =>
        notes
          .deleteByName(msg.source, name)
          .flatMap(_ => reply("deleted note").void)
      case _ =>
        reply("Invalid argument. Usage: /n <templateName>").void
    }
  }
}
