package com.novatecgmbh.eventsourcing.axon.application.config

import java.util.concurrent.CancellationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks

@Configuration
class TemporaryWorkaroundConfig {
  // source: https://github.com/rsocket/rsocket-java/issues/1018#issuecomment-954459392
  // workaround for: https://github.com/rsocket/rsocket-java/issues/1018
  @Autowired
  fun configureHooks() {
    Hooks.onErrorDropped {
      if (it is CancellationException || it.cause is CancellationException) {
        LOGGER.trace("Operator called default onErrorDropped", it)
      } else {
        LOGGER.error("Operator called default onErrorDropped", it)
      }
    }
  }

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(TemporaryWorkaroundConfig::class.java)
  }
}
