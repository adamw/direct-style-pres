package scalar

import ox.supervised
import sttp.client4.quick.*

@main def sseClient(): Unit =
  supervised {
    basicRequest
      .post(uri"http://localhost:51823/sse/echo3")
      .body("1234567890")
      .response(asInputStreamAlways { is =>
        parseSse(is).foreach(el => println(s"XXX: $el"))
        ()
      })
      .send()
      .body
  }
