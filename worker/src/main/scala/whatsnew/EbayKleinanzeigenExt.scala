package whatsnew

import cats.MonadThrow
import cats.effect._
import cats.implicits._

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.typelevel.log4cats.Logger
import sttp.client3.SttpBackend
import sttp.client3._
import sttp.model.Uri
import whatsnew.Entities._
import whatsnew.Entities._

class EbayKleinanzeigenExt[F[_]: Logger](backend: SttpBackend[F, Any])(implicit
    me: MonadThrow[F]
) extends ItemExtractor[F] {

  override def getAllItems(url: SearchUrl): F[List[Item]] = {
    Logger[F].info(s"Requesting elements for $url") *>
      Uri
        .parse(url.value)
        .fold(
          t => me.raiseError(new Throwable(s"unable to parse uri $t")),
          _.pure[F]
        )
        .flatMap(u =>
          basicRequest
            .get(u)
            .send(backend)
            .flatMap(
              _.body.fold(
                b =>
                  me.raiseError[List[Item]](
                    new NoSuchElementException(s"Empty body or error status $b")
                  ),
                body => parseItems(u)(body).pure[F]
              )
            )
        )
  }

  private def parseItems(uri: Uri)(s: String): List[Item] = {
    val browser = JsoupBrowser()
    val doc = browser.parseString(s)
    (for {
      article <- doc >> elements("#srchrslt-adtable article")
      id <- article >?> attr("data-adid")
      main <- article >?> element(".aditem-main")
      title <- main >> element("h2") >?> text
      description <- main >> element("p") >?> text
      rel <- (main >> element("h2") >?> element("a") >?> attr("href"))
        .orElse(main >> element("h2") >?> element("span") >?> attr("data-url"))
      abs <- (uri.scheme, uri.host, rel).mapN {
        case (schema, host, relPath) => schema + "://" + host + relPath
      }
      url <- validateUrl(abs).toOption
      image = (article >> element(".imagebox") >> element("img") >?> attr(
          "src"
        )).flatMap(s => validateUrl(s).toOption)
    } yield Item(id, url, title, description, image)).toList
  }
}
