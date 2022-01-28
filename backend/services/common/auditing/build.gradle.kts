plugins {
    id("com.novatecgmbh.commons-kotlin")
}

group = "${group}.common"

dependencies {
    implementation("com.novatecgmbh.eventsourcing.axon.common:api")

    implementation("org.axonframework:axon-modelling")
    implementation("org.springframework.boot:spring-boot-starter-security")
}