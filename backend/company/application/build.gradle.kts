plugins { id("com.novatecgmbh.web-module") }

group = "${group}.company"

dependencies {
  implementation(project(":api"))
  implementation(project(":command-query"))
  implementation(project(":web"))

  implementation("com.novatecgmbh.eventsourcing.axon.common:auditing")
  implementation("com.novatecgmbh.eventsourcing.axon.common:web")

  implementation("org.flywaydb:flyway-core")
  implementation("org.postgresql:postgresql")

  testImplementation("com.h2database:h2")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
