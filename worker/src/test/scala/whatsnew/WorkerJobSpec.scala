package whatsnew

import java.time.ZonedDateTime

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect._
import cats.effect.kernel.Ref
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.unsafe.IORuntime

import eu.timepit.refined.auto._
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.slf4j.Slf4jLogger
import whatsnew.CoreEntities.SearchJob
import whatsnew.Entities._

class WorkerJobSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with BeforeAndAfter {

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]
  val item = Item("id", "https://itemurl", "title", "desc", None)

  class TestSetup(oldItemIds: List[String], newItems: List[Item]) {

    val searchJob: SearchJob = SearchJob(
      url = "https://host/search",
      chatId = 1L,
      created = ZonedDateTime.now(),
      runs = 0
    )

    val testProgram = for {
      updatedJobs <- Ref.empty[IO, List[SearchJob]]
      updatedItemIds <- Ref.empty[IO, List[String]]
      updatedItems <- Ref.empty[IO, List[Item]]
      searchesAlg = new SearchesAlg[IO] {
        override def getAll: IO[List[SearchJob]] = IO(List(searchJob))
        override def update(s: SearchJob): IO[SearchJob] =
          updatedJobs
            .update(u => u :+ s)
            .map(_ => s)
      }
      itemIdsAlg = new ItemIdAlg[IO] {

        override def add(chatId: Long, searchUrl: ItemUrl, ids: Set[String]): IO[Set[String]] =
          updatedItemIds
            .update(u => u ++ ids.toList)
            .map(_ => ids)

        override def get(
            chatId: Long,
            searchUrl: ItemUrl
        ): IO[Set[String]] = IO(oldItemIds.toSet)

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

  "The WorkerJob" should "should not return items in the first run" in {
    val testSetup = new TestSetup( oldItemIds = List.empty, newItems = List(item)) 
    for {
      refs <- testSetup.testProgram
      items <- refs._3.get
    } yield items.isEmpty shouldBe true
  }

  "The WorkerJob" should "set new ids after a run" in { 
    val testSetup = new TestSetup( oldItemIds = List.empty, newItems = List(item)) 
   for {
      refs <- testSetup.testProgram
      ids <- refs._2.get
    } yield ids.head shouldBe item.id
  }
  

  "The WorkerJob" should "notify the user about a new item" in { 
    val testSetup = new TestSetup( oldItemIds = List("prev"), newItems = List(item)) 
    for {
      refs <- testSetup.testProgram
      items <- refs._3.get
    } yield items shouldBe List(item)
  }
  "The WorkerJob" should "not notify the user about the same item" in {
    val testSetup = new TestSetup( oldItemIds = List(item.id), newItems = List(item))
    for {
      refs <- testSetup.testProgram
      items <- refs._3.get
    } yield items shouldBe List.empty
  }
  "The WorkerJob" should "not notify the user about the same item if exracted twice" in {
    val testSetup = new TestSetup( oldItemIds = List(item.id), newItems = List(item, item))
    for {
      refs <- testSetup.testProgram
      items <- refs._3.get
    } yield items shouldBe List.empty
  }

  "The WorkerJob" should "increment runs for the job" in {
    val testSetup = new TestSetup( oldItemIds = List.empty, newItems = List(item)) 
    for {
      refs <- testSetup.testProgram
      jobs <- refs._1.get
    } yield jobs.head.runs shouldBe 1
  }
  
}
