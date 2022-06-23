package com.novatecgmbh.eventsourcing.mobile

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}