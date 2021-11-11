plugins {
    id("com.novatecgmbh.spring-boot")
}

dependencies {
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.opentracing.contrib:opentracing-spring-jaeger-starter")
    implementation("org.axonframework:axon-spring-boot-starter")
    implementation("org.axonframework.extensions.kotlin:axon-kotlin")
    implementation("org.axonframework.extensions.tracing:axon-tracing-spring-boot-starter")

    testImplementation ("org.axonframework:axon-test")
}
