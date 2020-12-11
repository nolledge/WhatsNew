package whatsnew

import whatsnew.Entities.Item

trait Responder[F[_]] {
  def sendNotification(chatId: Long, items: List[Item]): F[Unit]
}
