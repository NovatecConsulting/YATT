plugins {
    id("com.novatecgmbh.spring-boot")
}

dependencies {
    implementation("org.axonframework:axon-spring-boot-starter")
    implementation("org.axonframework.extensions.kotlin:axon-kotlin")

    testImplementation ("org.axonframework:axon-test")
}
