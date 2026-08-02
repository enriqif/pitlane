package com.widoo.pitlane.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

object SmartNotificationScheduler {

    // Verificar service por km — se corre cada vez que el usuario actualiza el km
    fun scheduleServiceKmCheck(
        context: Context,
        currentKm: Int,
        nextServiceKm: Int,
        vehicleName: String
    ) {
        if (nextServiceKm <= 0) return
        val kmRemaining = nextServiceKm - currentKm

        // Notificar cuando faltan 500km o menos
        if (kmRemaining in 1..500) {
            val inputData = workDataOf(
                ServiceReminderWorker.KEY_TYPE to ServiceReminderWorker.TYPE_SERVICE_KM,
                ServiceReminderWorker.KEY_TITLE to "🔧 Service próximo — $vehicleName",
                ServiceReminderWorker.KEY_MESSAGE to
                        "Faltan solo $kmRemaining km para el próximo service. " +
                        "¡Empezá a coordinar el turno!",
                ServiceReminderWorker.KEY_NOTIFICATION_ID to 2001
            )

            val request = OneTimeWorkRequestBuilder<ServiceReminderWorker>()
                .setInputData(inputData)
                .setInitialDelay(2, TimeUnit.SECONDS) // pequeño delay para no interrumpir al usuario
                .addTag("service_km_check")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "service_km_check",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    // Recordatorio mensual para actualizar km
    fun scheduleMonthlyKmReminder(context: Context) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val delay = cal.timeInMillis - System.currentTimeMillis()

        val inputData = workDataOf(
            ServiceReminderWorker.KEY_TYPE to ServiceReminderWorker.TYPE_MONTHLY_KM,
            ServiceReminderWorker.KEY_TITLE to "📍 ¿Cuántos km tiene tu auto?",
            ServiceReminderWorker.KEY_MESSAGE to
                    "Actualizá el kilometraje de tu vehículo para mantener " +
                    "tus recordatorios de mantenimiento precisos.",
            ServiceReminderWorker.KEY_NOTIFICATION_ID to 2002
        )

        val request = OneTimeWorkRequestBuilder<ServiceReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("monthly_km_reminder")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "monthly_km_reminder",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    // Alerta cuando el precio del combustible sube
    fun checkFuelPriceIncrease(
        context: Context,
        currentPrice: Double,
        previousPrice: Double,
        station: String
    ) {
        if (previousPrice <= 0) return
        val increasePercent = ((currentPrice - previousPrice) / previousPrice) * 100

        // Notificar si subió más del 5%
        if (increasePercent >= 5) {
            val inputData = workDataOf(
                ServiceReminderWorker.KEY_TYPE to ServiceReminderWorker.TYPE_FUEL_PRICE,
                ServiceReminderWorker.KEY_TITLE to "⛽ El combustible subió un ${
                    String.format("%.1f", increasePercent)
                }%",
                ServiceReminderWorker.KEY_MESSAGE to
                        "En $station pagaste $${String.format("%.0f", currentPrice)}/L " +
                        "vs $${String.format("%.0f", previousPrice)}/L la vez anterior. " +
                        "Considerá cargar en otras estaciones.",
                ServiceReminderWorker.KEY_NOTIFICATION_ID to 2003
            )

            val request = OneTimeWorkRequestBuilder<ServiceReminderWorker>()
                .setInputData(inputData)
                .setInitialDelay(3, TimeUnit.SECONDS)
                .addTag("fuel_price_check")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "fuel_price_check",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelAllWorkByTag("service_km_check")
            cancelAllWorkByTag("monthly_km_reminder")
            cancelAllWorkByTag("fuel_price_check")
        }
    }
}