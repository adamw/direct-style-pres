package scalar

import java.security.MessageDigest
import java.util.HexFormat

case class Question(question: String):
  val hash = HexFormat
    .of()
    .formatHex(
      MessageDigest.getInstance("MD5").digest(question.toLowerCase.getBytes)
    )

case class Answer(answer: String)
