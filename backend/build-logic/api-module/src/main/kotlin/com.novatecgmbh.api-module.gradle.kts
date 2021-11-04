plugins {
    id("com.novatecgmbh.commons-kotlin")
    kotlin("plugin.jpa")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.axonframework:axon-modelling")
    implementation("javax.persistence:javax.persistence-api")
    implementation("org.springframework.boot:spring-boot-starter-security")
}


