package whatsnew

import cats.effect._
import cats.implicits._

import com.bot4s.telegram.cats.TelegramBot
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.ChatId
import sttp.client3.SttpBackend
import whatsnew.Entities._

class TelegramResponder[F[_]: Async](
    token: String,
    backend: SttpBackend[F, Any]
) extends TelegramBot(token = token, backend = backend)
    with Responder[F] {

  def sendNotification(chatId: Long, items: List[Item]): F[Unit] =
    items
      .map(i => request(SendMessage(ChatId.fromChat(chatId), i.show)))
      .sequence
      .map(_ => ())

}
