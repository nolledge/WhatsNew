package whatsnew

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect._
import eu.timepit.refined.auto._

import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import org.scalatest.BeforeAndAfterEach
import cats.effect.testing.scalatest.AsyncIOSpec

class RedisItemIdIntTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with Matchers
    with BeforeAndAfterEach {

  val redisUrl = "redis://localhost"

  val chatId = 1L
  val searchUrl = Entities.validateUrl("http://scrape-me.com").right.get
  val items = Set("1", "2")
  val moreItems = Set("3", "4")

  override protected def beforeEach(): Unit =
    Redis[IO]
      .utf8(redisUrl)
      .use { cmd =>
        cmd.del(s"items:$chatId:$searchUrl")
      }

  val redisItemIds = new RedisItemIdInt[IO](redisUrl)

  "The RedisItemIdInt" should "add empty" in {
    (for {
      res <- redisItemIds.add(chatId, searchUrl, Set.empty)
    } yield res.isEmpty shouldBe true)
  }
  "The RedisItemIdInt" should "should add/get item ids" in {
    (for {
      _ <- redisItemIds.add(chatId, searchUrl, items)
      res <- redisItemIds.get(chatId, searchUrl)
    } yield res shouldBe items)
  }
  "The RedisItemIdInt" should "should append when added" in {
    (for {
      _ <- redisItemIds.add(chatId, searchUrl, moreItems)
      _ <- redisItemIds.add(chatId, searchUrl, items)
      res <- redisItemIds.get(chatId, searchUrl)
    } yield res shouldBe items ++ moreItems)
  }
  "The RedisItemIdInt" should "return empty when nothing set" in {
    (for {
      res <- redisItemIds.get(chatId, searchUrl)
    } yield res.isEmpty shouldBe true)
  }

}
