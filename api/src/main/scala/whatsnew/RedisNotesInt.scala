package whatsnew

import cats.effect._
import cats.implicits._

import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.effect.Log.Stdout._
import eu.timepit.refined.auto._
import whatsnew.Entities._

class RedisNotesInt[F[_]: Async](redisUrl: String) extends NotesAlg[F] {

  def add(n: Note): F[Unit] =
    Redis[F].utf8(redisUrl).use { implicit cmd =>
      for {
        _ <- cmd.set(s"chats:${n.chatId}:notes:${n.name}", n.text)
        _ <- cmd.sAdd(s"chats:${n.chatId}:notes", n.name)
      } yield ()
    }

  def getByName(chatId: Long, name: String): F[Option[Entities.Note]] =
    Redis[F].utf8(redisUrl).use { implicit cmd =>
      for {
        maybeText <- cmd.get(s"chats:${chatId}:notes:${name}")
      } yield maybeText.map(t => Note(chatId, name, t))
    }

  def deleteByName(chatId: Long, name: String): F[Unit] =
    Redis[F].utf8(redisUrl).use { implicit cmd =>
      for {
        _ <- cmd.del(s"chats:${chatId}:notes:${name}")
        _ <- cmd.sRem(s"chats:${chatId}:notes", name)
      } yield ()
    }

  def allNames(chatId: Long): F[Set[String]] =
    Redis[F].utf8(redisUrl).use { implicit cmd =>
      cmd.sMembers(s"chats:${chatId}:notes")
    }

}
