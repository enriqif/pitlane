package com.widoo.pitlane.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.car.app.connection.CarConnection
import androidx.core.app.NotificationCompat
import com.widoo.pitlane.AutoServiceApp
import com.widoo.pitlane.MainActivity
import com.widoo.pitlane.R
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Observa la conexión con Android Auto (proyección desde el teléfono o Android Automotive
 * nativo) mientras el proceso de la app esté vivo, y sugiere iniciar la medición de viaje
 * apenas se conecta — sin tener que abrir la app y buscar el botón manualmente.
 *
 * Limitación conocida: solo detecta la conexión mientras el proceso de la app sigue vivo
 * (Android no permite escuchar esto indefinidamente en background sin un foreground service
 * dedicado). En uso normal el proceso suele seguir cacheado, así que alcanza para el caso
 * típico de "subís al auto con el teléfono en el bolsillo".
 */
object CarConnectionMonitor {
    private const val NOTIFICATION_ID = 3002
    private var wasConnected = false
    private var started = false
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start(context: Context) {
        if (started) return
        started = true

        val appContext = context.applicationContext
        CarConnection(appContext).type.observeForever { connectionType ->
            val isConnected = connectionType != CarConnection.CONNECTION_TYPE_NOT_CONNECTED
            if (isConnected && !wasConnected) {
                onConnected(appContext)
            }
            wasConnected = isConnected
        }
    }

    private fun onConnected(context: Context) {
        if (TripTracker.state.value.isTracking) return // ya se está midiendo un viaje

        scope.launch {
            val db = AppDatabase.getInstance(context)
            val vehicle = VehicleRepository(db.vehicleDao()).getActive().first() ?: return@launch
            showPromptNotification(context, vehicle.id)
        }
    }

    private fun showPromptNotification(context: Context, vehicleId: Long) {
        val startIntent = Intent(context, TripTrackingService::class.java).apply {
            action = TripTrackingService.ACTION_START
            putExtra(TripTrackingService.EXTRA_VEHICLE_ID, vehicleId)
        }
        val startPendingIntent = PendingIntent.getForegroundService(
            context, 0, startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AutoServiceApp.TRIP_PROMPT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚗 Auto conectado")
            .setContentText("¿Medimos este viaje para actualizar el odómetro?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Iniciar viaje", startPendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
