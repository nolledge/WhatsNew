package whatsnew

import whatsnew.CoreEntities.SearchJob
import cats.implicits._
import cats.effect._
import Entities._
import scala.concurrent.duration._
import io.chrisdavenport.log4cats.Logger

class WorkerJob[F[_]: Concurrent: Timer: Logger](
    searchJobs: SearchesAlg[F],
    itemIds: ItemIdAlg[F],
    itemExtractor: ItemExtractor[F],
    responder: Responder[F],
    requestInterval: FiniteDuration
) {

  def runJob(): F[Unit] =
    Logger[F].info("Starting worker job") *>
      fs2.Stream
        .evals(searchJobs.getAll)
        .evalMap(s => searchAndDiff(s).map(diff => (s.chatId, diff)))
        .evalMap((notifyUser _).tupled)
        .delayBy(requestInterval)
        .compile
        .drain

  private def searchAndDiff(s: SearchJob): F[List[Item]] =
    for {
      _ <- Logger[F].debug(s"Receiving items for searchJob $s")
      prev <- itemIds.get(s.chatId, s.url).map(_.toList)
      current <- itemExtractor.getAllItems(s.url).map(_.distinct)
      _ <- Logger[F].debug(
        s"${prev.length} previous items and ${current.length} current items"
      )
      currentIds = current.map(_.id)
      diffIds =
        if (prev.isEmpty) { List.empty }
        else { currentIds.diff(prev) }
      _ <- itemIds.set(s.chatId, s.url, currentIds.toSet)
      _ <- searchJobs.update(s.copy(runs = s.runs + 1))
    } yield current.filter(c => diffIds.contains(c.id))

  private def notifyUser(chatId: Long, items: List[Item]): F[Unit] =
    Logger[F].debug(s"Found items $items for chat $chatId") *>
      responder.sendNotification(chatId, items)

}
