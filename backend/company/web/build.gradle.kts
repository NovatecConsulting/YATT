plugins {
    id("com.novatecgmbh.web-module")
}

group = "${group}.company"

dependencies {
    api(project(":api"))

    implementation("com.novatecgmbh.eventsourcing.axon.common:web")
}