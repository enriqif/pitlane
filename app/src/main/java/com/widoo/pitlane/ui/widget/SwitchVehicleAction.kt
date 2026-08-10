package com.widoo.pitlane.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.flow.first

class SwitchVehicleAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val db = AppDatabase.getInstance(context)
        val vehicleRepo = VehicleRepository(db.vehicleDao())

        val vehicles = vehicleRepo.getAll().first()
        if (vehicles.size <= 1) return

        val activeIndex = vehicles.indexOfFirst { it.isActive }
        val nextIndex = (activeIndex + 1) % vehicles.size
        vehicleRepo.setActive(vehicles[nextIndex].id)

        // Pequeño delay para que Room confirme el cambio


        kotlinx.coroutines.delay(300)

        // Usar updateAll en lugar de update por ID
        LargeWidget().updateAll(context)
        SmallWidget().updateAll(context)
        // Forzar refresh de todos los widgets
//        val manager = GlanceAppWidgetManager(context)
//
//        manager.getGlanceIds(LargeWidget::class.java).forEach { id ->
//            LargeWidget().update(context, id)
//        }
//        manager.getGlanceIds(SmallWidget::class.java).forEach { id ->
//            SmallWidget().update(context, id)
//        }
    }
}