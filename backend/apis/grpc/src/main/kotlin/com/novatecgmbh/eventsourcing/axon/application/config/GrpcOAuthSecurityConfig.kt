package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.ProjectServiceGrpc
import com.novatecgmbh.eventsourcing.axon.TaskServiceGrpc
import com.novatecgmbh.eventsourcing.axon.UserServiceGrpc
import com.novatecgmbh.eventsourcing.axon.application.security.CustomUserAuthenticationConverter
import io.grpc.reflection.v1alpha.ServerReflectionGrpc
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader
import net.devh.boot.grpc.server.security.check.AccessPredicate
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.AccessDecisionManager
import org.springframework.security.access.AccessDecisionVoter
import org.springframework.security.access.vote.UnanimousBased
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OAuth2ResourceServerProperties::class)
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class GrpcOAuthSecurityConfig : GlobalMethodSecurityConfiguration() {

  @Bean
  fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder {
    return JwtDecoders.fromOidcIssuerLocation(properties.jwt.issuerUri)
  }

  @Bean
  fun customUserAuthenticationConverter(userDetailsService: UserDetailsService) =
      CustomUserAuthenticationConverter(userDetailsService)

  @Bean
  fun jwtAuthenticationProvider(
      decoder: JwtDecoder,
      jwtAuthenticationConverter: CustomUserAuthenticationConverter
  ) =
      JwtAuthenticationProvider(decoder).apply {
        setJwtAuthenticationConverter(jwtAuthenticationConverter)
      }

  @Bean
  fun authenticationManager(
      jwtAuthenticationProvider: JwtAuthenticationProvider
  ): AuthenticationManager = ProviderManager(jwtAuthenticationProvider)

  @Bean
  fun grpcAuthenticationReader(jwtAuthenticationProvider: JwtAuthenticationProvider) =
      BearerAuthenticationReader {
    BearerTokenAuthenticationToken(it)
  }

  @Bean
  fun grpcSecurityMetadataSource(): GrpcSecurityMetadataSource? =
      ManualGrpcSecurityMetadataSource().apply {
        set(ServerReflectionGrpc.getServerReflectionInfoMethod(), AccessPredicate.authenticated())
        set(ProjectServiceGrpc.getFindMyProjectsMethod(), AccessPredicate.authenticated())
        set(TaskServiceGrpc.getGetTasksByProjectMethod(), AccessPredicate.authenticated())
        set(TaskServiceGrpc.getSubscribeTasksByProjectMethod(), AccessPredicate.authenticated())
        set(UserServiceGrpc.getFindAllMethod(), AccessPredicate.authenticated())
        setDefault(AccessPredicate.denyAll())
      }

  @Bean
  override fun accessDecisionManager(): AccessDecisionManager =
      UnanimousBased(ArrayList<AccessDecisionVoter<*>>().apply { add(AccessPredicateVoter()) })
}
