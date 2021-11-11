plugins {
    id("com.novatecgmbh.axon-spring-boot-application")
}

group = "${group}.project"

dependencies {
    implementation(project(":api"))
    implementation(project(":command-query"))

    implementation("com.novatecgmbh.eventsourcing.axon.common:auditing")

    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql")

    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}