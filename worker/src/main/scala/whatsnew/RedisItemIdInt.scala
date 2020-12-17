package whatsnew

import dev.profunktor.redis4cats.Redis
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.effect.Log.Stdout._

class RedisItemIdInt[F[_]: Concurrent: ContextShift](redisUrl: String)
    extends ItemIdAlg[F] {

  override def get(chatId: Long, searchUrl: Entities.ItemUrl): F[Set[String]] =
    Redis[F].utf8(redisUrl).use { cmd =>
      cmd.sMembers(s"items:$chatId:$searchUrl")
    }

  override def set(
      chatId: Long,
      searchUrl: Entities.ItemUrl,
      ids: Set[String]
  ): F[Set[String]] =
    Redis[F].utf8(redisUrl).use { cmd =>
      for {
        _ <- cmd.del(s"items:$chatId:$searchUrl")
        _ <- cmd.sAdd(s"items:$chatId:$searchUrl", ids.toSeq: _*)
      } yield ids
    }

}
