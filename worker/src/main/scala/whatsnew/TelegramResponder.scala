package whatsnew

import com.bot4s.telegram.cats.TelegramBot
import cats.effect.ContextShift
import cats.effect._
import cats.implicits._
import com.bot4s.telegram.methods.SendMessage
import Entities._
import com.bot4s.telegram.models.ChatId
import com.softwaremill.sttp.SttpBackend

class TelegramResponder[F[_]: Async: ContextShift](
    token: String,
    backend: SttpBackend[F, Nothing]
) extends TelegramBot(token = token, backend = backend)
    with Responder[F] {

  def sendNotification(chatId: Long, items: List[Item]): F[Unit] =
    items
      .map(i => request(SendMessage(ChatId.fromChat(chatId), i.show)))
      .sequence
      .map(_ => ())

}
