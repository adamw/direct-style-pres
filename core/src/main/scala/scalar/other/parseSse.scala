package scalar

import ox.{fork, repeatWhile, Ox}
import ox.channels.{Source, StageCapacity}
import sttp.model.sse.ServerSentEvent

import java.io.InputStream
import scala.annotation.tailrec

def parseSse(is: InputStream)(using Ox): Source[ServerSentEvent] =
  val chunks = StageCapacity.newChannel[Array[Byte]]
  fork {
    try
      repeatWhile {
        val a = new Array[Byte](1024)
        val r = is.read(a)
        if r == -1 then
          chunks.done()
          false
        else
          chunks.send(a.take(r))
          true
      }
    catch case t: Throwable => chunks.errorSafe(t)
  }

  chunks
    .mapStatefulConcat(() => Array.empty[Byte]) { case (buffer, nextChunk) =>
      @tailrec
      def splitChunksAtNewLine(buf: Array[Byte], chunk: Array[Byte], acc: Vector[Array[Byte]])
          : (Array[Byte], Vector[Array[Byte]]) =
        val newlineIdx = chunk.indexOf('\n')
        if newlineIdx == -1 then (buf ++ chunk, acc)
        else
          val (chunk1, chunk2) = chunk.splitAt(newlineIdx + 1)
          splitChunksAtNewLine(Array.empty[Byte], chunk2, acc :+ (buffer ++ chunk1))

      val (newBuffer, toEmit) = splitChunksAtNewLine(buffer, nextChunk, Vector.empty)

      (newBuffer, toEmit)
    }
    .mapAsView(new String(_))
    .mapStatefulConcat(() => Vector.empty[String]) { case (acc, el) =>
      if el.isBlank then (Vector.empty, Some(acc)) else (acc :+ el.dropRight(1), Nil)
    }
    .map(lines => ServerSentEvent.parse(lines.asInstanceOf[Vector[String]].toList))
