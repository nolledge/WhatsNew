package whatsnew

final case class Config(redis: Config.Redis, bot: Config.Bot)

object Config {
  final case class Redis(url: String)
  final case class Bot(token: String)
}
