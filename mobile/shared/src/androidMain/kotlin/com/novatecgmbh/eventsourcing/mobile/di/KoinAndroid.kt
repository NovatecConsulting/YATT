package com.novatecgmbh.eventsourcing.mobile.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.russhwolf.settings.AndroidSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { createSharedPreferences(get()) }
    single<Settings> { AndroidSettings(get()) }
}

fun createSharedPreferences(context: Context): SharedPreferences =
    context.getSharedPreferences("app", MODE_PRIVATE)