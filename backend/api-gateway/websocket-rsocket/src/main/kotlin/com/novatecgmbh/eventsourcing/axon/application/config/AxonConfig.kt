package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_KEYS
import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_USER_ID_META_DATA_KEY
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.messaging.ReactorMessageDispatchInterceptor
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.axonframework.queryhandling.QueryMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import reactor.core.publisher.Mono

@Configuration
class AxonConfig {

  @Autowired
  fun reactiveCommandGateway(queryGateway: ReactorCommandGateway) {
    queryGateway.registerDispatchInterceptor(UserInjectingCommandMessageDispatchInterceptor())
  }

  @Autowired
  fun reactiveQueryGateway(queryGateway: ReactorQueryGateway) {
    queryGateway.registerDispatchInterceptor(UserInjectingQueryMessageDispatchInterceptor())
  }

  @Bean
  fun correlationDataProviders(): CorrelationDataProvider =
      MultiCorrelationDataProvider<CommandMessage<*>>(
          listOf(
              SimpleCorrelationDataProvider(*AUDIT_KEYS),
              MessageOriginProvider(),
          ))
}

class UserInjectingQueryMessageDispatchInterceptor :
    ReactorMessageDispatchInterceptor<QueryMessage<*, *>> {
  override fun intercept(messageMono: Mono<QueryMessage<*, *>>): Mono<QueryMessage<*, *>> =
      ReactiveSecurityContextHolder.getContext()
          .switchIfEmpty(Mono.error(RuntimeException("ReactiveSecurityContext is empty")))
          .flatMap {
            val auth: Authentication? = it.authentication
            if (auth == null) {
              Mono.error(RuntimeException("Authentication is null!"))
            } else {
              val principal = auth.principal
              if (principal is RegisteredUserProfile) {
                messageMono.map { message ->
                  message.andMetaData(
                      mutableMapOf<String, String>(
                          AUDIT_USER_ID_META_DATA_KEY to principal.identifier.toString(),
                      ))
                }
              } else {
                messageMono
              }
            }
          }
}

class UserInjectingCommandMessageDispatchInterceptor :
    ReactorMessageDispatchInterceptor<CommandMessage<*>> {
  override fun intercept(messageMono: Mono<CommandMessage<*>>): Mono<CommandMessage<*>> =
      ReactiveSecurityContextHolder.getContext()
          .switchIfEmpty(Mono.error(RuntimeException("ReactiveSecurityContext is empty")))
          .flatMap {
            val auth: Authentication? = it.authentication
            if (auth == null) {
              Mono.error(RuntimeException("Authentication is null!"))
            } else {
              val principal = auth.principal
              if (principal is RegisteredUserProfile) {
                messageMono.map { message ->
                  message.andMetaData(
                      mutableMapOf<String, String>(
                          AUDIT_USER_ID_META_DATA_KEY to principal.identifier.toString(),
                      ))
                }
              } else {
                messageMono
              }
            }
          }
}
