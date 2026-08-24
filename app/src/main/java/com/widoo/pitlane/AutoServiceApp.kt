package com.widoo.pitlane

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.widoo.pitlane.di.appModule
import com.widoo.pitlane.service.CarConnectionMonitor
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
        createNotificationChannels()
        SmartNotificationScheduler.scheduleMonthlyKmReminder(this)
        CarConnectionMonitor.start(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val remindersChannel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de mantenimiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de vencimiento y mantenimiento de tu vehículo"
            }

            val tripChannel = NotificationChannel(
                TRIP_CHANNEL_ID,
                "Viaje en curso",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación persistente mientras se mide un viaje por GPS"
            }

            val tripPromptChannel = NotificationChannel(
                TRIP_PROMPT_CHANNEL_ID,
                "Sugerencia de viaje",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sugerencia para medir un viaje al conectar con Android Auto"
            }

            manager.createNotificationChannel(remindersChannel)
            manager.createNotificationChannel(tripChannel)
            manager.createNotificationChannel(tripPromptChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pitlane_reminders"
        const val TRIP_CHANNEL_ID = "pitlane_trip_tracking"
        const val TRIP_PROMPT_CHANNEL_ID = "pitlane_trip_prompt"
    }
}
