package whatsnew

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._
import eu.timepit.refined._
import eu.timepit.refined.string.Url._
import _root_.cats.Show

object Entities {

  type ImageUrl = String Refined Url
  type ItemUrl = String Refined Url
  type SearchUrl = String Refined Url

  final case class Item(
      id: String,
      url: ItemUrl,
      tile: String,
      description: String,
      image: Option[ImageUrl]
  )

  implicit val showItem: Show[Item] = (i: Item) => s"${i.tile}\n${i.url}"

  def validateUrl(s: String): Either[String, ImageUrl] = refineV(s)
}
