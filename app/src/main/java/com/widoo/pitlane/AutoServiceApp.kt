package com.widoo.pitlane

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.widoo.pitlane.di.appModule
import com.widoo.pitlane.worker.SmartNotificationScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AutoServiceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AutoServiceApp)
            modules(appModule)
        }
        createNotificationChannel()
        SmartNotificationScheduler.scheduleMonthlyKmReminder(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de mantenimiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de vencimiento y mantenimiento de tu vehículo"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pitlane_reminders"
    }
}