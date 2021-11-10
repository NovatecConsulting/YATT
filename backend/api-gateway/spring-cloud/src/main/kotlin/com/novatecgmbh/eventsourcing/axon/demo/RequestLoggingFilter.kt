package com.novatecgmbh.eventsourcing.axon.demo

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(2)
class RequestLoggingFilter : GlobalFilter {

  override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> =
      chain
          .filter(exchange)
          .doOnError {
            LOGGER.warn(
                "Exception on Request: {} {}: {}",
                exchange.request.method,
                exchange.request.path,
                it.message)
          }
          .doOnSuccess {
            if (ServerWebExchangeUtils.isAlreadyRouted(exchange)) {
              LOGGER.info(
                  "{} {} {} routed to {}",
                  exchange.response.statusCode,
                  exchange.request.method,
                  exchange.request.path,
                  exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR))
            } else {
              LOGGER.info(
                  "Route not found: {} {} {}",
                  exchange.response.statusCode,
                  exchange.request.method,
                  exchange.request.path)
            }
          }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
  }
}
