package com.novatecgmbh.eventsourcing.axon.application.config

import graphql.language.StringValue
import graphql.schema.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer

@Configuration
class GraphQlConfiguration {

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
}
