plugins {
  id("com.novatecgmbh.commons-kotlin")
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  kotlin("plugin.spring")
}

group = "${group}.apis"

dependencies {
  implementation("com.novatecgmbh.eventsourcing.axon.common:auditing")
  implementation("com.novatecgmbh.eventsourcing.axon.company:api")
  implementation("com.novatecgmbh.eventsourcing.axon.project:api")
  implementation("com.novatecgmbh.eventsourcing.axon.user:api")
  implementation("com.novatecgmbh.eventsourcing.axon.apis:api-common")

  implementation("io.opentracing.contrib:opentracing-spring-jaeger-cloud-starter")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.5")

  implementation("org.axonframework:axon-spring-boot-starter")
  implementation("org.axonframework.extensions.kotlin:axon-kotlin")
  implementation("org.axonframework.extensions.tracing:axon-tracing-spring-boot-starter")

  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.experimental:graphql-spring-boot-starter:1.0.0-M4")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

repositories {
  mavenCentral()
  maven("https://repo.spring.io/milestone")
}
