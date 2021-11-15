plugins {
  id("java-library")
  kotlin("jvm")
}

group = "com.novatecgmbh.eventsourcing.axon"

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(platform("com.novatecgmbh.platform:product-platform"))
  testImplementation(platform("com.novatecgmbh.platform:test-platform"))

  implementation(kotlin("reflect"))
  implementation(kotlin("stdlib-jdk8"))

  testImplementation("com.tngtech.archunit:archunit-junit5")
  testImplementation("io.mockk:mockk")
  testImplementation("org.apache.commons:commons-lang3")
  testImplementation("org.junit.jupiter:junit-jupiter-api")

  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Copy> { duplicatesStrategy = DuplicatesStrategy.WARN }

tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.WARN }
