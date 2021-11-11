plugins {
    id("java-platform")
}

group = "com.novatecgmbh.platform"

// allow the definition of dependencies to other platforms like the Axon BOM
javaPlatform.allowDependencies()

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:2.5.6"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:2020.0.4"))
    api(platform("org.axonframework:axon-bom:4.5.5"))

    constraints {
        api("io.opentracing.contrib:opentracing-spring-jaeger-starter:3.2.2")
        api("io.opentracing.contrib:opentracing-spring-jaeger-web-starter:3.2.2")
        api("io.projectreactor:reactor-core:3.4.8")
        api("javax.persistence:javax.persistence-api:2.2")
    }
}
