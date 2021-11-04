plugins {
    id("com.novatecgmbh.commons-kotlin")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

dependencies {
    implementation("org.axonframework:axon-spring-boot-starter")
    implementation("org.axonframework.extensions.kotlin:axon-kotlin")
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation ("org.axonframework:axon-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
