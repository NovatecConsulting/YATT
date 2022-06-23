package com.novatecgmbh.eventsourcing.mobile.di

import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.Settings
import org.koin.core.KoinApplication
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

fun initKoinIos(): KoinApplication = initKoin()

actual val platformModule = module {
    single<Settings> { AppleSettings(NSUserDefaults.standardUserDefaults()) }
}