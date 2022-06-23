package com.novatecgmbh.eventsourcing.mobile.android

import android.app.Application
import com.novatecgmbh.eventsourcing.mobile.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MainApplication)
        }
    }
}