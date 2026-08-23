package com.widoo.pitlane.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.widoo.pitlane.AutoServiceApp
import com.widoo.pitlane.MainActivity
import com.widoo.pitlane.R
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.local.entity.TripEntity
import com.widoo.pitlane.data.repository.TripRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground service que mide la distancia recorrida por GPS mientras dura un viaje,
 * para sugerir después una actualización del kilometraje del vehículo activo.
 * No requiere ningún hardware extra — usa el GPS del propio teléfono.
 */
class TripTrackingService : Service() {

    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null
    private var startedAt: Long = 0L
    private var vehicleId: Long = -1L
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val locationListener = LocationListener { location -> onNewLocation(location) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }

        vehicleId = intent?.getLongExtra(EXTRA_VEHICLE_ID, -1L) ?: -1L
        startTracking()
        return START_STICKY
    }

    private fun startTracking() {
        startedAt = System.currentTimeMillis()
        lastLocation = null
        TripTracker.start(startedAt)

        val notification = buildNotification(0.0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this, NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_MS,
                MIN_DISTANCE_M,
                locationListener
            )
        } catch (e: SecurityException) {
            stopTracking()
        }
    }

    private fun onNewLocation(location: Location) {
        if (location.accuracy > MAX_ACCURACY_METERS) return

        val previous = lastLocation
        if (previous != null) {
            val distance = previous.distanceTo(location)
            val elapsedSeconds = (location.time - previous.time) / 1000.0
            val speedMs = if (elapsedSeconds > 0) distance / elapsedSeconds else 0.0

            // Filtra saltos irreales de GPS (ruido) en vez de sumarlos como distancia real
            if (speedMs <= MAX_SPEED_MS) {
                TripTracker.addDistance(distance.toDouble())
                updateNotification(TripTracker.state.value.distanceMeters)
            }
        }
        lastLocation = location
    }

    private fun stopTracking() {
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }

        val distance = TripTracker.state.value.distanceMeters
        val start = startedAt
        val vId = vehicleId

        if (distance >= MIN_TRIP_DISTANCE_METERS) {
            serviceScope.launch {
                val db = AppDatabase.getInstance(applicationContext)
                val resolvedVehicleId = if (vId > 0) vId
                else VehicleRepository(db.vehicleDao()).getActive().first()?.id ?: -1L

                if (resolvedVehicleId > 0) {
                    TripRepository(db.tripDao()).insert(
                        TripEntity(
                            vehicleId = resolvedVehicleId,
                            distanceMeters = distance,
                            startedAt = start,
                            endedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        TripTracker.stop()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }

    private fun buildNotification(distanceMeters: Double): Notification {
        val stopIntent = Intent(this, TripTrackingService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val km = distanceMeters / 1000.0
        return NotificationCompat.Builder(this, AutoServiceApp.TRIP_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Viaje en curso")
            .setContentText("%.1f km recorridos".format(km))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Finalizar viaje", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(distanceMeters: Double) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(distanceMeters))
    }

    companion object {
        const val ACTION_START = "com.widoo.pitlane.action.START_TRIP"
        const val ACTION_STOP = "com.widoo.pitlane.action.STOP_TRIP"
        const val EXTRA_VEHICLE_ID = "vehicle_id"

        private const val NOTIFICATION_ID = 3001
        private const val MIN_TIME_MS = 5000L
        private const val MIN_DISTANCE_M = 15f
        private const val MAX_ACCURACY_METERS = 30f
        private const val MAX_SPEED_MS = 55.0 // ~200 km/h, descarta saltos de GPS
        private const val MIN_TRIP_DISTANCE_METERS = 300.0

        fun start(context: Context, vehicleId: Long) {
            val intent = Intent(context, TripTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_VEHICLE_ID, vehicleId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, TripTrackingService::class.java).apply { action = ACTION_STOP }
            )
        }
    }
}
