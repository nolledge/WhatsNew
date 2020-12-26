package whatsnew

import Entities._
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp._
import cats.effect._
import cats.implicits._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import Entities._
import io.chrisdavenport.log4cats.Logger

class EbayKleinanzeigenExt[F[_]: Logger](backend: SttpBackend[F, Nothing])(
    implicit me: MonadThrow[F]
) extends ItemExtractor[F] {

  override def getAllItems(url: SearchUrl): F[List[Item]] = {
    Logger[F].info(s"Requesting elements for $url") *>
      Uri
        .parse(url.value)
        .fold(t => me.raiseError(t), _.pure[F])
        .flatMap(u =>
          backend
            .send(sttp.get(u))
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
      rel <- main >> element("a") >?> attr("href")
      abs = uri.scheme + "://" + uri.host + rel
      url <- validateUrl(abs).toOption
      title <- main >> element("h2") >?> text
      description <- main >> element("p") >?> text
      image = (article >> element(".imagebox") >?> attr(
          "data-imgsrc"
        )).flatMap(s => validateUrl(s).toOption)
    } yield Item(id, url, title, description, image)).toList
  }
}
