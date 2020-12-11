package whatsnew

import CoreEntities._
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.effect.Log.Stdout._
import java.time.ZonedDateTime
import java.time.ZonedDateTime
import eu.timepit.refined.auto._
import scala.util.Try

class RedisSearchesInt[F[_]: Concurrent: ContextShift](redisUrl: String)(
    implicit val me: MonadThrow[F]
) {

  /**
    * Create a SearchJob by adding a SarchUrl
    *
    * @param chatId
    * @param s
    * @return
    */
  def create(chatId: Long, s: SearchUrl): F[SearchJob] =
    Redis[F].utf8(redisUrl).use { implicit cmd =>
      for {
        _ <- cmd.sAdd("chats", chatId.toString)
        r <- addOrUpdate(SearchJob(s, chatId, ZonedDateTime.now(), 0))
      } yield r
    }

  /**
    * Create a SearchJob by adding a SarchUrl
    *
    * @param chatId
    * @param s
    * @return
    */
  def update(s: SearchJob): F[SearchJob] =
    Redis[F].utf8(redisUrl).use { implicit cmd =>
      addOrUpdate(s)
    }

  private def addOrUpdate(
      s: SearchJob
  )(implicit cmd: RedisCommands[F, String, String]): F[SearchJob] = {
    val chatId = s.chatId
    for {
      _ <- cmd.sAdd(s"searches:$chatId", s.url.value)
      _ <-
        cmd
          .set(
            s"searches:$chatId:${urlToId(s.url)}:created",
            s.created.toString
          )
      _ <- cmd.set(s"searches:$chatId:${urlToId(s.url)}:runs", s.runs.toString)
    } yield s
  }

  /**
    * Returns all SearchJobs for a given user, identified by chatId
    *
    * @param chatId
    * @return
    */
  def getByChat(chatId: Long): F[List[SearchJob]] =
    Redis[F].utf8(redisUrl).use { cmd =>
      for {
        urls <- cmd.sMembers(s"searches:$chatId")
        validatedUrls = urls.flatMap(v => validateUrl(v).toOption)
        results <-
          validatedUrls.toList.map(extractSearchJob(cmd, chatId)).sequence
      } yield results
    }

  /**
    * Collects metadata for a given SearchUrl and combines it to a 'SearchJob'
    *
    * @param cmd
    * @param chatId
    * @param url
    * @return
    */
  private def extractSearchJob(
      cmd: RedisCommands[F, String, String],
      chatId: Long
  )(
      url: SearchUrl
  ): F[SearchJob] =
    for {
      created <-
        cmd
          .get(s"searches:$chatId:${urlToId(url)}:created")
          .flatMap(getOrRaiseError[String])
          .map(ZonedDateTime.parse)
      runs <-
        cmd
          .get(s"searches:$chatId:${urlToId(url)}:runs")
          .flatMap(getOrRaiseError[String])
          .map(_.toLong)
    } yield SearchJob(url, chatId, created, runs)

  /**
    * Helper method to extract option value or raise an error when not set
    *
    * @param maybeT
    * @return
    */
  private def getOrRaiseError[T](maybeT: Option[T]): F[T] =
    maybeT.fold(
      me.raiseError[T](new IllegalStateException("data inconsistency in redis"))
    )(_.pure[F])

  /**
    * The URL is part of the key in redis and should not container any colons
    *
    * @param url
    * @return
    */
  private def urlToId(url: SearchUrl): String = url.value.replaceAll(":", "")

  def deleteAll(chatId: Long): F[Unit] =
    Redis[F].utf8(redisUrl).use { cmd =>
      for {
        _ <- cmd.del(s"searches:$chatId")
        userKeys <- cmd.keys(s"searches:$chatId:*")
        _ <- userKeys.map(k => cmd.del(k)).sequence
        _ <- cmd.sRem("searches", chatId.toString)
      } yield ()
    }

  def deleteUrl(chatId: Long, u: SearchUrl): F[Unit] =
    Redis[F].utf8(redisUrl).use { cmd =>
      for {
        _ <- cmd.del(s"searches:$chatId:${u.value}:created")
        _ <- cmd.del(s"searches:$chatId:${u.value}:runs")
        _ <- cmd.sRem(s"searches:$chatId", u.value)
        searches <- cmd.sMembers(s"searches:$chatId").map(_.size)
        _ <-
          if (searches == 0) {
            cmd.sRem("searches", u.value)
          } else { ().pure[F] }
      } yield ()
    }

  /**
    * Returns all SearchJobs for a given user, identified by chatId
    *
    * @param chatId
    * @return
    */
  def getAll: F[List[SearchJob]] =
    Redis[F].utf8(redisUrl).use { cmd =>
      for {
        chatIdsS <- cmd.sMembers(s"chats")
        chatIds = chatIdsS.flatMap(s => Try(s.toLong).toOption)
        results <- chatIds.toList.map(id => getByChat(id)).sequence
      } yield results.flatten
    }

}
