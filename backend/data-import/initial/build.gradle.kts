plugins {
  id("com.novatecgmbh.commons-kotlin")
  id("io.spring.dependency-management")
  kotlin("plugin.spring")
}

group = "${group}.initial-data-import"

dependencies {
  implementation("com.novatecgmbh.eventsourcing.axon.common:auditing")
  implementation("com.novatecgmbh.eventsourcing.axon.common:api")
  implementation("com.novatecgmbh.eventsourcing.axon.company:api")
  implementation("com.novatecgmbh.eventsourcing.axon.project:api")
  implementation("com.novatecgmbh.eventsourcing.axon.user:api")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.opentracing.contrib:opentracing-spring-jaeger-cloud-starter")
  implementation("io.projectreactor:reactor-core")
  implementation("org.axonframework:axon-spring-boot-starter")
  implementation("org.axonframework.extensions.kotlin:axon-kotlin")
  implementation("org.axonframework.extensions.tracing:axon-tracing-spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter")

  testImplementation("org.axonframework:axon-test")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
