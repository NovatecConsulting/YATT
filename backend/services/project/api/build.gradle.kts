plugins {
    id("com.novatecgmbh.commons-kotlin")
    kotlin("plugin.jpa")
}

group = "${group}.project"

dependencies {
    api("com.novatecgmbh.eventsourcing.axon.common:api")
    api("com.novatecgmbh.eventsourcing.axon.company:api")
    api("com.novatecgmbh.eventsourcing.axon.user:api")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.axonframework:axon-modelling")
    implementation("javax.persistence:javax.persistence-api")
}
