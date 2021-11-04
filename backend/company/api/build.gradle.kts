plugins {
    id("com.novatecgmbh.api-module")
}

group = "${group}.company"

dependencies {
    api("com.novatecgmbh.eventsourcing.axon.common:api")
    api("com.novatecgmbh.eventsourcing.axon.user:api")
}