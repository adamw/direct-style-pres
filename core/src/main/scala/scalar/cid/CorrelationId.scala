package scalar.cid

import org.slf4j.MDC
import ox.ForkLocal

import scala.util.Random

object CorrelationId:
  val HeaderName = "X-Correlation-ID"
  
  private val forkLocal = ForkLocal[String]("GLOBAL")

  private val random = new Random()
  private def generate(): String = {
    def randomUpperCaseChar() = (random.nextInt(91 - 65) + 65).toChar
    def segment = (1 to 3).map(_ => randomUpperCaseChar()).mkString
    s"$segment-$segment"
  }

  def get(): String = forkLocal.get()

  def withCid[T](maybeCid: Option[String])(f: => T): T =
    val cid = maybeCid.getOrElse(generate())
    MDC.put("cid", cid)
    try forkLocal.scopedWhere(cid)(f)
    finally MDC.remove("cid")
