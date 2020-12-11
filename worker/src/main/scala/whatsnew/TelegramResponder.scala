package whatsnew

import com.bot4s.telegram.cats.TelegramBot
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import cats.effect.ContextShift
import cats.effect._
import cats.implicits._
import com.bot4s.telegram.methods.SendMessage
import Entities._
import com.bot4s.telegram.models.ChatId

class TelegramResponder[F[_]: Async: ContextShift](token: String)
    extends TelegramBot(token = token, backend = AsyncHttpClientCatsBackend())
    with Responder[F] {

  def sendNotification(chatId: Long, items: List[Item]): F[Unit] =
    items
      .map(i => request(SendMessage(ChatId.fromChat(chatId), i.show)))
      .sequence
      .map(_ => ())

}
