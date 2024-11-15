package whatsnew

import cats.effect._
import cats.implicits._

import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.effect.Log.Stdout._

class RedisItemIdInt[F[_]: Async](redisUrl: String)
    extends ItemIdAlg[F] {

  override def get(chatId: Long, searchUrl: Entities.ItemUrl): F[Set[String]] =
    Redis[F].utf8(redisUrl).use { cmd =>
      cmd.sMembers(s"items:$chatId:$searchUrl")
    }

  override def add(
      chatId: Long,
      searchUrl: Entities.ItemUrl,
      ids: Set[String]
  ): F[Set[String]] =
    Redis[F].utf8(redisUrl).use { cmd =>
      (if (ids.isEmpty) {
         ().pure[F]
       } else {
         cmd.sAdd(s"items:$chatId:$searchUrl", ids.toSeq: _*)
       }).map(_ => ids)
    }
}
