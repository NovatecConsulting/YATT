plugins {
    id("com.novatecgmbh.web-module")
}

group = "${group}.api-gateway"

dependencies {
    implementation("com.novatecgmbh.eventsourcing.axon.common:auditing")
    implementation("com.novatecgmbh.eventsourcing.axon.common:web")
    implementation("com.novatecgmbh.eventsourcing.axon.company:web")
    implementation("com.novatecgmbh.eventsourcing.axon.project:web")
    implementation("com.novatecgmbh.eventsourcing.axon.user:web")
}
