package whatsnew

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.implicits._
import cats.effect._
import eu.timepit.refined.auto._

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.BeforeAndAfter
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.scalatest.BeforeAndAfterAll

class EbayKleinanzeigenExtTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfter {

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  val kleinanzeigenExt: Resource[IO, EbayKleinanzeigenExt[IO]] =
    AsyncHttpClientCatsBackend
      .resource[IO]()
      .map(new EbayKleinanzeigenExt[IO](_))

  "The EbayKleinanzeigenExt" should "increment the counter with update function" in {
    kleinanzeigenExt
      .use(
        _.getAllItems(
          "https://www.ebay-kleinanzeigen.de/s-berlin/thinkpad/k0l3331"
        )
      )
      .map { results =>
        results.isEmpty shouldBe false
        results.find(_.image.isDefined).isDefined shouldBe true
      }
  }
}
