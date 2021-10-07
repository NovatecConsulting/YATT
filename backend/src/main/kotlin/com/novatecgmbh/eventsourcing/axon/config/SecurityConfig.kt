package com.novatecgmbh.eventsourcing.axon.config

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver
import org.keycloak.adapters.springsecurity.KeycloakConfiguration
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy

@KeycloakConfiguration
internal class SecurityConfig : KeycloakWebSecurityConfigurerAdapter() {
  @Autowired
  @Throws(Exception::class)
  fun configureGlobal(auth: AuthenticationManagerBuilder) {
    auth.authenticationProvider(
        keycloakAuthenticationProvider().apply {
          this.setGrantedAuthoritiesMapper(SimpleAuthorityMapper())
        })
  }

  @Bean
  fun keycloakConfigResolver(): KeycloakSpringBootConfigResolver =
      KeycloakSpringBootConfigResolver()

  @Bean
  override fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy =
      RegisterSessionAuthenticationStrategy(SessionRegistryImpl())

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    super.configure(http)
    http.authorizeRequests().anyRequest().authenticated()
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    http.cors().disable()
  }
}
