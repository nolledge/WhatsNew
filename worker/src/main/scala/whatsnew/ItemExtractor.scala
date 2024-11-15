package whatsnew

import whatsnew.Entities._

trait ItemExtractor[F[_]] {
  def getAllItems(url: SearchUrl): F[List[Item]]
}
