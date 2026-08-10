package com.widoo.pitlane.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeWidget()

    companion object {
        const val ACTION_SWITCH = "com.widoo.pitlane.SWITCH_VEHICLE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("LargeWidgetReceiver", ">>> onReceive: ${intent.action}")
        super.onReceive(context, intent)

        if (intent.action == ACTION_SWITCH) {
            android.util.Log.d("LargeWidgetReceiver", ">>> SWITCH action detected!")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val vehicleRepo = VehicleRepository(db.vehicleDao())

                    val vehicles = vehicleRepo.getAll().first()
                    android.util.Log.d("LargeWidgetReceiver", ">>> vehicles: ${vehicles.size}")

                    if (vehicles.size > 1) {
                        val activeIndex = vehicles.indexOfFirst { it.isActive }
                        val nextIndex = (activeIndex + 1) % vehicles.size
                        android.util.Log.d("LargeWidgetReceiver", ">>> switching $activeIndex -> $nextIndex")
                        vehicleRepo.setActive(vehicles[nextIndex].id)
                    }
                    LargeWidget().updateAll(context)
                    SmallWidget().updateAll(context)

// Forzar redibujado via AppWidgetManager
                    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                    val componentName = android.content.ComponentName(context, LargeWidgetReceiver::class.java)
                    val ids = appWidgetManager.getAppWidgetIds(componentName)
                    android.util.Log.d("LargeWidgetReceiver", ">>> forcing redraw for ${ids.size} widgets")
                    appWidgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list)

// Enviar broadcast de update forzado
                    val updateIntent = Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                        putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                        component = componentName
                    }
                    context.sendBroadcast(updateIntent)

                    android.util.Log.d("LargeWidgetReceiver", ">>> widgets updated!")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}