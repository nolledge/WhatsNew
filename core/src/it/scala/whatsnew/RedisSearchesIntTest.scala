package whatsnew

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect._
import CoreEntities._
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext
import org.scalatest.BeforeAndAfter
import org.scalactic.source.Position

class RedisSearchesIntTest
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfter {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val redisSearches = new RedisSearchesInt[IO]("redis://localhost")
  val searchUrl: SearchUrl = "https://www.ebay-kleinanzeigen.de/s-thinkpad/k0"
  val searchUrl2: SearchUrl = "https://www.ebay-kleinanzeigen.de/s-something/k0"
  val chatId: Long = 1L

  override protected def before(fun: => Any)(implicit pos: Position): Unit = {
    redisSearches.deleteAll(1L).unsafeRunSync()
  }

  "The RedisSearchesIntTest" should "add a search job to redis and get it afterwards" in {
    (for {
      add <-
        redisSearches
          .create(
            chatId,
            searchUrl
          )
      received <-
        redisSearches
          .getByChat(chatId)
    } yield add shouldBe received.head).unsafeRunSync
  }

  "The RedisSearchesIntTest" should "delete all entries for a user" in {
    (for {
      _ <-
        redisSearches
          .create(
            chatId,
            searchUrl
          )
      _ <- redisSearches.deleteAll(chatId)
      received <-
        redisSearches
          .getByChat(chatId)
    } yield received.isEmpty shouldBe true).unsafeRunSync
  }
  "The RedisSearchesIntTest" should "delete by url" in {
    (for {
      s1 <-
        redisSearches
          .create(
            chatId,
            searchUrl
          )
      _ <-
        redisSearches
          .create(
            chatId,
            searchUrl2
          )
      _ <- redisSearches.deleteUrl(chatId, searchUrl2)
      received <-
        redisSearches
          .getByChat(chatId)
    } yield received.head shouldBe s1).unsafeRunSync
  }

  "The RedisSearchesIntTest" should "increment the counter with update function" in {
    (for {
      _ <- redisSearches.create(chatId, searchUrl) // create
      received1 <- redisSearches.getByChat(chatId).map(_.head) // receive
      _ <-
        redisSearches.update(received1.copy(runs = received1.runs + 1)) //update
      received2 <-
        redisSearches.getByChat(chatId).map(_.head) // receive updated
    } yield received2.runs shouldBe 1).unsafeRunSync
  }

  "The RedisSearchesIntTest" should "not create another job when updating" in {
    (for {
      _ <- redisSearches.create(chatId, searchUrl) // create
      received1 <- redisSearches.getByChat(chatId).map(_.head) // receive
      _ <-
        redisSearches.update(received1.copy(runs = received1.runs + 1)) //update
      received2 <- redisSearches.getByChat(chatId)
    } yield received2.size shouldBe 1).unsafeRunSync
  }
}
