plugins {
  id("com.novatecgmbh.commons-kotlin")
  kotlin("plugin.jpa")
}

group = "${group}.user"

dependencies {
  implementation("com.novatecgmbh.eventsourcing.axon.common:api")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("javax.persistence:javax.persistence-api")
  implementation("org.axonframework:axon-modelling")
}
