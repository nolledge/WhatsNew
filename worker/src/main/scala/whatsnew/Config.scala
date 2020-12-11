package whatsnew

import scala.concurrent.duration.FiniteDuration

final case class Config(redis: Config.Redis, bot: Config.Bot, job: Config.Job)

object Config {
  final case class Redis(url: String)
  final case class Bot(token: String)
  final case class Job(requestInterval: FiniteDuration)
}
