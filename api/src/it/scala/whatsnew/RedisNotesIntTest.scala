package whatsnew

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect._
import eu.timepit.refined.auto._

import scala.concurrent.ExecutionContext
import org.scalatest.BeforeAndAfter
import whatsnew.Entities.Note

class RedisNotesIntTest extends AnyFlatSpec with Matchers with BeforeAndAfter {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  val redisNotes = new RedisNotesInt[IO]("redis://localhost")
  val chatId: Long = 1L
  val noteName: String = "noteName"
  val noteText: String = "noteText"
  val sample: Note = Note(chatId, noteName, noteText)

  // override protected def before(fun: => Any)(implicit pos: Position): Unit = {
  // redisSearches.deleteAll(1L).unsafeRunSync()
  //}

  "The RedisNotesInt" should "add a note and receive it by name" in {
    (for {
      _ <- redisNotes.add(sample)
      byName <- redisNotes.getByName(chatId, noteName)
    } yield sample shouldBe byName.head).unsafeRunSync
  }

  "The RedisNotesInt" should "delete a note by name" in {
    (for {
      _ <- redisNotes.add(sample)
      _ <- redisNotes.deleteByName(chatId, noteName)
      byName <- redisNotes.getByName(chatId, noteName)
    } yield byName.isEmpty shouldBe true).unsafeRunSync
  }

  "The RedisNotesInt" should "list all note names" in {
    (for {
      _ <- redisNotes.add(sample)
      names <- redisNotes.allNames(chatId)
    } yield names shouldBe Set(noteName)).unsafeRunSync
  }

  "The RedisNotesInt" should "not return any names when empty" in {
    (for {
      _ <- redisNotes.deleteByName(chatId, noteName)
      names <- redisNotes.allNames(chatId)
    } yield names.isEmpty shouldBe true).unsafeRunSync
  }

}
