package whatsnew

import whatsnew.CoreEntities._

trait SearchesAlg[F[_]] {
  def getAll: F[List[SearchJob]]
  def getByChat(chatId: Long): F[List[SearchJob]]
  def create(chatid: Long, s: SearchUrl): F[SearchJob]
  def update(s: SearchJob): F[SearchJob]
  def deleteAll(chatId: Long): F[Unit]
  def deleteUrl(chatId: Long, u: SearchUrl): F[Unit]
}
