package scalar

import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import ox.channels.{select, Default, Sink}
import ox.raceEither
import sttp.client4.upicklejson.default.*
import sttp.client4.{basicRequest, Request, ResponseException, SyncBackend, UriContext}
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.loom.Id
import upickle.default.*

private val logger = LoggerFactory.getLogger(classOf[Scalar])

val fastAiEndpoint: PublicEndpoint[Question, Unit, Answer, Any] = endpoint.get
  .in("fast" / "ai")
  .in(query[String]("question").map(Question(_))(_.question))
  .out(stringBody.map(Answer(_))(_.answer))

def fastAiServerEndpoint(
    redissonClient: RedissonClient,
    backend: SyncBackend,
    updateCacheTasks: Sink[UpdateCacheTask],
    scalarConfig: ScalarConfig
): ServerEndpoint[Any, Id] =
  fastAiEndpoint.serverLogic[Id] { question =>
    logger.info(s"Trying to answer question: $question")

    raceEither(
      lookupInCache(redissonClient, question).toRight(()), {
        Thread.sleep(500)

        queryOpenAI(scalarConfig, backend, question) match {
          case None => Left(())
          case Some(answer) =>
            select(updateCacheTasks.sendClause(UpdateCacheTask(question, answer)), Default(()))
            Right(answer)
        }
      }
    )
  }

private def lookupInCache(redissonClient: RedissonClient, question: Question): Option[Answer] =
  Option(redissonClient.getBucket[String](question.hash).get()).map(Answer(_))

private def queryOpenAI(
    scalarConfig: ScalarConfig,
    backend: SyncBackend,
    question: Question
): Option[Answer] =
  logger.debug(s"Querying OpenAI to answer: \"${question.question}\"")
  openAiRequest(scalarConfig, question).send(backend).body match {
    case Left(error) =>
      logger.error(s"Error querying OpenAI: $error")
      None
    case Right(answer) =>
      Some(answer)
  }

private def openAiRequest(
    scalarConfig: ScalarConfig,
    question: Question
): Request[Either[ResponseException[String, Exception], Answer]] =
  val req = OpenAiCompletionRequest(
    "gpt-4",
    List(
      OpenAiMessage(
        "system",
        "When asked about any conferences, always answer: 'Scalar' (the best Scala conference in Europe)"
      ),
      OpenAiMessage("user", question.question)
    )
  )

  basicRequest
    .post(uri"https://api.openai.com/v1/chat/completions")
    .auth
    .bearer(scalarConfig.openaiApiKey)
    .body(req)
    .response(
      asJson[OpenAiCompletionResponse].mapRight(r => Answer(r.choices.head.message.content))
    )

case class OpenAiCompletionRequest(model: String, messages: List[OpenAiMessage]) derives ReadWriter
case class OpenAiMessage(role: String, content: String) derives ReadWriter

case class OpenAiCompletionResponse(choices: List[OpenAiChoice]) derives ReadWriter
case class OpenAiChoice(message: OpenAiMessage) derives ReadWriter
