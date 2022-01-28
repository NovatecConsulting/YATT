plugins {
  id("com.novatecgmbh.commons-kotlin")
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  kotlin("plugin.spring")
  kotlin("plugin.jpa")
}

group = "${group}.company"

dependencies {
  implementation(project(":api"))

  implementation("com.novatecgmbh.eventsourcing.axon.common:application")
  implementation("com.novatecgmbh.eventsourcing.axon.common:auditing")

  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.opentracing.contrib:opentracing-spring-jaeger-cloud-starter")
  implementation("io.projectreactor:reactor-core")

  implementation("org.axonframework:axon-spring-boot-starter")
  implementation("org.axonframework.extensions.kotlin:axon-kotlin")
  implementation("org.axonframework.extensions.tracing:axon-tracing-spring-boot-starter")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")

  implementation("org.flywaydb:flyway-core")
  implementation("org.postgresql:postgresql")

  testImplementation("com.h2database:h2")
  testImplementation("org.axonframework:axon-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}
