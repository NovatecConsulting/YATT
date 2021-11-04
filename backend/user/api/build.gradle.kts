plugins {
    id("com.novatecgmbh.api-module")
}

group = "${group}.user"

dependencies {
    api("com.novatecgmbh.eventsourcing.axon.common:api")
}