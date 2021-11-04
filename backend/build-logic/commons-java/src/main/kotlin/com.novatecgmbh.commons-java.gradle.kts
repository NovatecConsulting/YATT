plugins {
    id("java")
    id("java-library")
}

group = "com.novatecgmbh.eventsourcing.axon"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(platform("com.novatecgmbh.platform:product-platform"))

    testImplementation(platform("com.novatecgmbh.platform:test-platform"))
    testImplementation ("com.tngtech.archunit:archunit-junit5:0.21.0")
    testImplementation("org.apache.commons:commons-lang3:3.11")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
