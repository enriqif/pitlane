package com.widoo.pitlane.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.widoo.pitlane.service.CarConnectionMonitor

/**
 * Después de reiniciar el teléfono (o de actualizar la app) el proceso arranca de cero y
 * [CarConnectionMonitor] deja de escuchar. Si el usuario tenía activado el seguimiento
 * automático de viajes, lo volvemos a armar acá para no depender de que abra la app antes
 * de subirse al auto.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // CarConnectionMonitor decide solo si tiene que hacer algo según la
                // preferencia del usuario; acá solo lo dejamos escuchando.
                CarConnectionMonitor.start(context.applicationContext)
            }
        }
    }
}
