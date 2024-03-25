package scalar

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*

case class ScalarConfig(openaiApiKey: String) derives ConfigReader

object ScalarConfig:
  def load(): ScalarConfig = ConfigSource.default.loadOrThrow[ScalarConfig]
