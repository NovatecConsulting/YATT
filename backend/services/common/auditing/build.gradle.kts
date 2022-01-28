plugins {
    id("com.novatecgmbh.commons-kotlin")
}

group = "${group}.common"

dependencies {
    api(project(":api"))

    implementation("org.axonframework:axon-modelling")
    implementation("org.springframework.boot:spring-boot-starter-security")
}