package whatsnew

import whatsnew.CoreEntities._

trait SearchesAlg[F[_]] {
  def getAll: F[List[SearchJob]]
  def update(s: SearchJob): F[SearchJob]
}
