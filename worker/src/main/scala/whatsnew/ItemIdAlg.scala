package whatsnew

import Entities._

trait ItemIdAlg[F[_]] {

  def get(chatId: Long, searchUrl: ItemUrl): F[Set[String]]
  def add(chatId: Long, searchUrl: ItemUrl, ids: Set[String]): F[Set[String]]
}
