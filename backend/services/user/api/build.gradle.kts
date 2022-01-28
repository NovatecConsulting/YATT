plugins {
  id("com.novatecgmbh.commons-kotlin")
  kotlin("plugin.jpa")
}

group = "${group}.user"

dependencies {
  api("com.novatecgmbh.eventsourcing.axon.common:api")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.axonframework:axon-modelling")
  implementation("javax.persistence:javax.persistence-api")
}
