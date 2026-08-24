package com.widoo.pitlane.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import com.widoo.pitlane.data.local.AppDatabase
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.data.repository.VehicleRepository
import com.widoo.pitlane.service.TripTracker
import com.widoo.pitlane.service.TripTrackingService
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla única mostrada en Android Auto: vehículo activo + botón para
 * iniciar/finalizar la medición de viaje por GPS.
 */
class TripCarScreen(carContext: CarContext) : Screen(carContext) {

    private val vehicleRepository = VehicleRepository(AppDatabase.getInstance(carContext).vehicleDao())
    private var vehicle: VehicleEntity? = null
    private var isTracking = false

    init {
        lifecycleScope.launch {
            vehicleRepository.getActive().collect {
                vehicle = it
                invalidate()
            }
        }
        lifecycleScope.launch {
            TripTracker.state.collect {
                isTracking = it.isTracking
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val numFormat = NumberFormat.getNumberInstance(Locale("es", "AR"))
        val v = vehicle

        val rowBuilder = Row.Builder()
        if (v != null) {
            rowBuilder
                .setTitle("${v.brand} ${v.model}")
                .addText("${numFormat.format(v.currentKm)} km")
        } else {
            rowBuilder.setTitle("Sin vehículo activo")
        }

        val paneBuilder = Pane.Builder()
            .addRow(rowBuilder.build())

        if (v != null) {
            val actionTitle = if (isTracking) "Finalizar viaje" else "Iniciar viaje"
            paneBuilder.addAction(
                Action.Builder()
                    .setTitle(actionTitle)
                    .setOnClickListener {
                        if (isTracking) {
                            TripTrackingService.stop(carContext)
                        } else {
                            TripTrackingService.start(carContext, v.id)
                        }
                    }
                    .build()
            )
        }

        val header = Header.Builder()
            .setStartHeaderAction(Action.APP_ICON)
            .setTitle("Pitlane")
            .build()

        return PaneTemplate.Builder(paneBuilder.build())
            .setHeader(header)
            .build()
    }
}
