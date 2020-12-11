package whatsnew

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect._
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext
import org.scalatest.BeforeAndAfter
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class EbayKleinanzeigenExtTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfter {

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val backend = AsyncHttpClientCatsBackend[IO]()
  val kleinanzeigenExt = new EbayKleinanzeigenExt[IO](backend)

  "The EbayKleinanzeigenExt" should "increment the counter with update function" in {
    val results = kleinanzeigenExt
      .getAllItems(
        "https://www.ebay-kleinanzeigen.de/s-berlin/thinkpad/k0l3331"
      )
      .unsafeRunSync()
    results.isEmpty shouldBe false
    results.find(_.image.isDefined).isDefined shouldBe true
  }
}
