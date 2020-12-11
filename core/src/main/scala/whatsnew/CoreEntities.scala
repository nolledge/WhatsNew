package whatsnew

import eu.timepit.refined.numeric._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._
import java.time.ZonedDateTime
import eu.timepit.refined._
import eu.timepit.refined.string.Url._

object CoreEntities {

  type SearchUrl = String Refined Url
  type SearchId = Int Refined NonNegative

  final case class SearchJob(
      url: SearchUrl,
      chatId: Long,
      created: ZonedDateTime,
      runs: Long
  )

  def validateUrl(s: String): Either[String, SearchUrl] = refineV(s)

  object Commands {
    sealed trait Command
    final case class AddSearch(url: SearchUrl) extends Command
    final case class DeleteSearch(id: SearchId) extends Command
    case object GetSearches extends Command
    case object DeleteAll extends Command
  }
}
