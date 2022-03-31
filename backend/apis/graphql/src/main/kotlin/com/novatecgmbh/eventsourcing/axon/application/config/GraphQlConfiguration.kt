package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.security.CustomUserAuthenticationConverter
import com.novatecgmbh.eventsourcing.axon.application.security.CustomUserDetailsService
import graphql.language.StringValue
import graphql.schema.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import org.springframework.graphql.web.WebGraphQlHandlerInterceptor
import org.springframework.graphql.web.WebGraphQlRequest
import org.springframework.graphql.web.WebGraphQlResponse
import org.springframework.graphql.web.WebSocketGraphQlHandlerInterceptor
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import reactor.core.publisher.Mono

@Configuration
class GraphQlConfiguration(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") val issuer: String
) {

  @Bean
  fun runtimeWiringConfigurer(): RuntimeWiringConfigurer = RuntimeWiringConfigurer { builder ->
    builder.scalar(dateScalar())
  }

  fun dateScalar(): GraphQLScalarType =
      GraphQLScalarType.newScalar()
          .name("Date")
          .description("Java 8 LocalDate as scalar.")
          .coercing(
              object : Coercing<LocalDate, String> {
                override fun serialize(dataFetcherResult: Any): String {
                  return (dataFetcherResult as? LocalDate)?.toString()
                      ?: throw CoercingSerializeException("Expected a LocalDate object.")
                }

                override fun parseValue(input: Any): LocalDate {
                  return try {
                    if (input is String) {
                      LocalDate.parse(input)
                    } else {
                      throw CoercingParseValueException("Expected a String")
                    }
                  } catch (e: DateTimeParseException) {
                    throw CoercingParseValueException(
                        String.format("Not a valid date: '%s'.", input), e)
                  }
                }

                override fun parseLiteral(input: Any): LocalDate {
                  return if (input is StringValue) {
                    try {
                      LocalDate.parse(input.value)
                    } catch (e: DateTimeParseException) {
                      throw CoercingParseLiteralException(e)
                    }
                  } else {
                    throw CoercingParseLiteralException("Expected a StringValue.")
                  }
                }
              })
          .build()

  @Bean
  fun interceptor(userDetailsService: CustomUserDetailsService) =
      CustomWebSocketGraphQlHandlerInterceptor(userDetailsService, issuer)
}

/**
 * Workaround for bug reported here:
 * https://github.com/spring-projects/spring-graphql/issues/342
 * Can be removed once the propagation works out-of-the-box
 */
class CustomWebSocketGraphQlHandlerInterceptor(
    private val userDetailsService: CustomUserDetailsService,
    private val issuer: String
) : WebSocketGraphQlHandlerInterceptor {

  override fun intercept(
      request: WebGraphQlRequest,
      chain: WebGraphQlHandlerInterceptor.Chain
  ): Mono<WebGraphQlResponse> {
    request.headers.getFirst(HttpHeaders.AUTHORIZATION)?.let { accessToken ->
      val authConverter = CustomUserAuthenticationConverter(userDetailsService)
      val jwt =
          JwtDecoders.fromIssuerLocation<JwtDecoder>(issuer)
              .decode(accessToken.replace("Bearer ", ""))
      val user = authConverter.convert(jwt)
      SecurityContextHolder.getContext().authentication = user
    }
    return super.intercept(request, chain)
  }
}
