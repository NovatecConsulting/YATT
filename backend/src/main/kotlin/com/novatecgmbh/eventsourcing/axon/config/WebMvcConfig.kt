package com.novatecgmbh.eventsourcing.axon.config

import com.novatecgmbh.eventsourcing.axon.security.UserPrincipalResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfiguration : WebMvcConfigurer {

  override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
    resolvers.add(UserPrincipalResolver())
  }
}
