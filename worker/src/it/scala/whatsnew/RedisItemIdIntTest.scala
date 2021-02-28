package whatsnew

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect._
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import org.scalatest.BeforeAndAfterEach

class RedisItemIdIntTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach {

  val redisUrl = "redis://localhost"

  val chatId = 1L
  val searchUrl = Entities.validateUrl("http://scrape-me.com").right.get
  val items = Set("1", "2")
  val moreItems = Set("3", "4")

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  override protected def beforeEach(): Unit =
    Redis[IO]
      .utf8(redisUrl)
      .use { cmd =>
        cmd.del(s"items:$chatId:$searchUrl")
      }
      .unsafeRunSync()

  val redisItemIds = new RedisItemIdInt[IO](redisUrl)

  "The RedisItemIdInt" should "add empty" in {
    (for {
      res <- redisItemIds.add(chatId, searchUrl, Set.empty)
    } yield res.isEmpty shouldBe true).unsafeRunSync
  }
  "The RedisItemIdInt" should "should add/get item ids" in {
    (for {
      _ <- redisItemIds.add(chatId, searchUrl, items)
      res <- redisItemIds.get(chatId, searchUrl)
    } yield res shouldBe items).unsafeRunSync
  }
  "The RedisItemIdInt" should "should append when added" in {
    (for {
      _ <- redisItemIds.add(chatId, searchUrl, moreItems)
      _ <- redisItemIds.add(chatId, searchUrl, items)
      res <- redisItemIds.get(chatId, searchUrl)
    } yield res shouldBe items ++ moreItems).unsafeRunSync
  }
  "The RedisItemIdInt" should "return empty when nothing set" in {
    (for {
      res <- redisItemIds.get(chatId, searchUrl)
    } yield res.isEmpty shouldBe true).unsafeRunSync
  }

}
