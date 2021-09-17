import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"
	kotlin("plugin.jpa") version "1.5.21"
}

group = "com.novatec-gmbh.eventsourcing"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

val axonVersion by extra("4.5.3")
val reactorVersion by extra("3.4.8")

dependencies {
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor:reactor-core:$reactorVersion")
	implementation("org.axonframework:axon-spring-boot-starter")
	implementation("org.axonframework.extensions.kotlin:axon-kotlin")
	implementation("org.flywaydb:flyway-core")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.postgresql:postgresql")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")

	testImplementation("com.h2database:h2")
	testImplementation ("com.tngtech.archunit:archunit-junit5:0.21.0")
	testImplementation("org.apache.commons:commons-lang3:3.11")
	testImplementation ("org.axonframework:axon-test")
	testImplementation("org.mockito:mockito-all:1.10.19")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("org.axonframework:axon-bom:$axonVersion")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
