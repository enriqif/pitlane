package com.widoo.pitlane

import android.app.Application
import com.widoo.pitlane.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AutoServiceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AutoServiceApp)
            modules(appModule)
        }
    }
}