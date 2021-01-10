package whatsnew

import Entities._

trait NotesAlg[F[_]] {
  def add(note: Note): F[Unit]
  def getByName(chatId: Long, name: String): F[Option[Note]]
  def deleteByName(chatId: Long, name: String): F[Unit]
  def allNames(chatId: Long): F[Set[String]]
}
