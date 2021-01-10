package whatsnew

object Entities {
  final case class Note(
      chatId: Long,
      name: String,
      text: String
  )
}
