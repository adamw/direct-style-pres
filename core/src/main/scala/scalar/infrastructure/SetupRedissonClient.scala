package scalar.infrastructure

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

import java.util.concurrent.Executors

object SetupRedissonClient:
  def start(): RedissonClient =
    val config = new Config()
    config.useSingleServer().setAddress("redis://127.0.0.1:6379")
    config.setNettyExecutor(Executors.newVirtualThreadPerTaskExecutor())
    Redisson.create(config)
