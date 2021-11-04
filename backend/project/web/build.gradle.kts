plugins {
    id("com.novatecgmbh.web-module")
}

group = "${group}.project"

dependencies {
    api(project(":api"))

    implementation("com.novatecgmbh.eventsourcing.axon.common:web")
}