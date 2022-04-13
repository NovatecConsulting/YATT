plugins {
  id("com.novatecgmbh.commons-kotlin")
  id("io.spring.dependency-management")
  kotlin("plugin.spring")
}

group = "${group}.apis"

dependencies {
  api("com.novatecgmbh.eventsourcing.axon.common:api")
  api("com.novatecgmbh.eventsourcing.axon.common:auditing")
  api("com.novatecgmbh.eventsourcing.axon.company:api")
  api("com.novatecgmbh.eventsourcing.axon.project:api")
  api("com.novatecgmbh.eventsourcing.axon.user:api")

  implementation("org.axonframework:axon-spring-boot-starter")
  implementation("org.axonframework.extensions.kotlin:axon-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
}
