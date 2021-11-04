plugins {
    id("com.novatecgmbh.api-module")
}

group = "${group}.project"

dependencies {
    api("com.novatecgmbh.eventsourcing.axon.common:api")
    api("com.novatecgmbh.eventsourcing.axon.company:api")
    api("com.novatecgmbh.eventsourcing.axon.user:api")
}