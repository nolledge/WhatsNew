package whatsnew

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect._
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext
import org.scalatest.BeforeAndAfter
import whatsnew.CoreEntities.SearchJob
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.effect.concurrent.Ref
import java.time.ZonedDateTime
import whatsnew.Entities.Item
import scala.concurrent.duration._

class WorkerJobSpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val item = Item("id", "https://itemurl", "title", "desc", None)

  abstract class TestSetup(oldItemIds: List[String], newItems: List[Item]) {

    val searchJob: SearchJob = SearchJob(
      url = "https://host/search",
      chatId = 1L,
      created = ZonedDateTime.now(),
      runs = 0
    )

    val testProgram = for {
      updatedJobs <- Ref.of[IO, List[SearchJob]](List.empty[SearchJob])
      updatedItemIds <- Ref.of[IO, List[String]](List.empty[String])
      updatedItems <- Ref.of[IO, List[Item]](List.empty[Item])
      searchesAlg = new SearchesAlg[IO] {
        override def getAll: IO[List[SearchJob]] = IO(List(searchJob))
        override def update(s: SearchJob): IO[SearchJob] =
          updatedJobs
            .update(u => u :+ s)
            .map(_ => s)
      }
      itemIdsAlg = new ItemIdAlg[IO] {
        override def get(
            chatId: Long,
            searchUrl: Entities.ItemUrl
        ): IO[Set[String]] = IO(oldItemIds.toSet)
        override def set(
            chatId: Long,
            searchUrl: Entities.ItemUrl,
            ids: Set[String]
        ): IO[Set[String]] =
          updatedItemIds
            .update(u => u ++ ids.toList)
            .map(_ => ids)
      }
      itemExtractor = new ItemExtractor[IO] {
        override def getAllItems(url: Entities.SearchUrl): IO[List[Item]] =
          IO(newItems)
      }
      responder = new Responder[IO] {
        override def sendNotification(
            chatId: Long,
            items: List[Item]
        ): IO[Unit] =
          updatedItems.update(u => u ++ items)
      }
      _ <- new WorkerJob[IO](
        searchesAlg,
        itemIdsAlg,
        itemExtractor,
        responder,
        1.second
      ).runJob()
    } yield (updatedJobs, updatedItemIds, updatedItems)

  }

  "The WorkerJob" should "should not return items in the first run" in new TestSetup(
    oldItemIds = List.empty,
    newItems = List(item)
  ) {
    (for {
      refs <- testProgram
      items <- refs._3.get
    } yield items.isEmpty shouldBe true).unsafeRunSync()
  }

  "The WorkerJob" should "set new ids after a run" in new TestSetup(
    oldItemIds = List.empty,
    newItems = List(item)
  ) {
    (for {
      refs <- testProgram
      ids <- refs._2.get
    } yield ids.head shouldBe item.id).unsafeRunSync()
  }

  "The WorkerJob" should "notify the user about a new item" in new TestSetup(
    oldItemIds = List("prev"),
    newItems = List(item)
  ) {
    (for {
      refs <- testProgram
      items <- refs._3.get
    } yield items shouldBe List(item)).unsafeRunSync()
  }
  "The WorkerJob" should "not notify the user about the same item" in new TestSetup(
    oldItemIds = List(item.id),
    newItems = List(item)
  ) {
    (for {
      refs <- testProgram
      items <- refs._3.get
    } yield items shouldBe List.empty).unsafeRunSync()
  }

  "The WorkerJob" should "increment runs for the job" in new TestSetup(
    oldItemIds = List.empty,
    newItems = List(item)
  ) {
    (for {
      refs <- testProgram
      jobs <- refs._1.get
    } yield jobs.head.runs shouldBe 1).unsafeRunSync()
  }
}
