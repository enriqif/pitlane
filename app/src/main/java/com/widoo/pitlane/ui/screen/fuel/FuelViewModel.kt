package com.widoo.pitlane.ui.screen.fuel

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import com.widoo.pitlane.ui.widget.LargeWidget
import com.widoo.pitlane.ui.widget.SmallWidget
import com.widoo.pitlane.worker.SmartNotificationScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class FuelComparisonData(
    val kmDiff: Int = 0,
    val costDiff: Double = 0.0,
    val costDiffPercent: Double = 0.0,
    val litersDiff: Double = 0.0,
    val pricePerLiterDiff: Double = 0.0,
    val previousTotal: Double = 0.0
)

data class AddFuelUiState(
    val selectedVehicleId: Long = -1L,      // ← nuevo
    val selectedVehicleName: String = "",    // ← nuevo
    val date: Long = System.currentTimeMillis(),
    val km: String = "",
    val liters: String = "",
    val pricePerLiter: String = "",
    val totalCost: String = "",
    val station: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    // Validation errors
    val kmError: String? = null,
    val litersError: String? = null,
    val priceError: String? = null,
    val totalError: String? = null
)

data class FuelUiState(
    val fuelLogs: List<FuelLogEntity> = emptyList(),
    val vehicle: VehicleEntity? = null,
    val monthlyTotal: Double = 0.0,
    val avgConsumption: Double = 0.0,
    val comparison: FuelComparisonData? = null
)

class FuelViewModel(
    private val fuelRepository: FuelRepository,
    private val vehicleRepository: VehicleRepository,
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FuelUiState> = vehicleRepository
        .getActive()
        .flatMapLatest { vehicle ->
            if (vehicle == null) return@flatMapLatest flowOf(FuelUiState())
            fuelRepository.getByVehicle(vehicle.id).map { logs ->
                FuelUiState(
                    fuelLogs = logs,
                    vehicle = vehicle,
                    monthlyTotal = calculateMonthlyTotal(logs),
                    avgConsumption = calculateAvgConsumption(logs),
                    comparison = buildComparison(logs)
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FuelUiState())

    private val _addState = MutableStateFlow(AddFuelUiState())
    val addState: StateFlow<AddFuelUiState> = _addState.asStateFlow()
    val vehicles: StateFlow<List<VehicleEntity>> = vehicleRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            vehicleRepository.getActive().collect { vehicle ->
                vehicle?.let {
                    if (_addState.value.selectedVehicleId == -1L) {
                        _addState.value = _addState.value.copy(
                            selectedVehicleId = it.id,
                            selectedVehicleName = "${it.brand} ${it.model}"
                        )
                    }
                }
            }
        }
    }

    fun onVehicleSelected(vehicle: VehicleEntity) {
        _addState.value = _addState.value.copy(
            selectedVehicleId = vehicle.id,
            selectedVehicleName = "${vehicle.brand} ${vehicle.model}"
        )
    }

    fun onKmChange(v: String) {
        _addState.value = _addState.value.copy(km = v, kmError = null)
    }

    fun onStationChange(v: String) {
        _addState.value = _addState.value.copy(station = v)
    }

    fun onLitersChange(v: String) {
        _addState.value = _addState.value.copy(liters = v, litersError = null)
        recalculateTotal()
    }

    fun onPricePerLiterChange(v: String) {
        _addState.value = _addState.value.copy(pricePerLiter = v, priceError = null)
        recalculateTotal()
    }

    fun onTotalCostChange(v: String) {
        _addState.value = _addState.value.copy(totalCost = v, totalError = null)
    }

    private fun recalculateTotal() {
        val liters = _addState.value.liters.toDoubleOrNull() ?: return
        val price = _addState.value.pricePerLiter.toDoubleOrNull() ?: return
        val total = liters * price
        _addState.value = _addState.value.copy(
            totalCost = String.format("%.2f", total),
            totalError = null
        )
    }

    // El monto es el único campo obligatorio — km, litros y precio son opcionales,
    // para poder cargar una recarga vieja de la que no te acordás el detalle exacto.
    fun validateAndCheck(): Boolean {
        val s = _addState.value
        var hasError = false

        if (s.km.isNotBlank()) {
            val km = s.km.toIntOrNull()
            if (km == null || km <= 0) {
                _addState.value = _addState.value.copy(kmError = "Ingresá un kilometraje válido")
                hasError = true
            }
        }

        if (s.liters.isNotBlank()) {
            val liters = s.liters.toDoubleOrNull()
            when {
                liters == null || liters <= 0 -> {
                    _addState.value = _addState.value.copy(
                        litersError = "Ingresá una cantidad válida"
                    )
                    hasError = true
                }
                liters > 200 -> {
                    _addState.value = _addState.value.copy(
                        litersError = "Cantidad de litros muy alta (máx 200L)"
                    )
                    hasError = true
                }
            }
        }

        if (s.pricePerLiter.isNotBlank()) {
            val price = s.pricePerLiter.toDoubleOrNull()
            if (price == null || price <= 0) {
                _addState.value = _addState.value.copy(priceError = "Ingresá un precio válido")
                hasError = true
            }
        }

        val total = s.totalCost.toDoubleOrNull()
        if (total == null || total <= 0) {
            _addState.value = _addState.value.copy(totalError = "Ingresá el monto gastado")
            hasError = true
        }

        return !hasError
    }

    fun isFormValid(): Boolean {
        val s = _addState.value
        return s.totalCost.isNotBlank() &&
                s.kmError == null &&
                s.litersError == null &&
                s.priceError == null &&
                s.totalError == null
    }

    private fun calculateMonthlyTotal(logs: List<FuelLogEntity>): Double {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val startOfMonth = cal.timeInMillis
        return logs.filter { it.date >= startOfMonth }.sumOf { it.totalCost }
    }

    private fun calculateAvgConsumption(logs: List<FuelLogEntity>): Double {
        val withData = logs.filter { it.km != null && it.liters != null }
        if (withData.size < 2) return 0.0
        val sorted = withData.sortedBy { it.km }
        val kmDiff = (sorted.last().km!! - sorted.first().km!!).toDouble()
        val totalLiters = sorted.dropLast(1).sumOf { it.liters!! }
        if (kmDiff <= 0) return 0.0
        return (totalLiters / kmDiff) * 100
    }

    private fun buildComparison(logs: List<FuelLogEntity>): FuelComparisonData? {
        if (logs.size < 2) return null
        val latest = logs[0]
        val previous = logs[1]
        if (latest.km == null || previous.km == null ||
            latest.liters == null || previous.liters == null ||
            latest.pricePerLiter == null || previous.pricePerLiter == null
        ) return null

        val costDiff = latest.totalCost - previous.totalCost
        val costDiffPercent = if (previous.totalCost > 0)
            (costDiff / previous.totalCost) * 100 else 0.0

        return FuelComparisonData(
            kmDiff = latest.km - previous.km,
            costDiff = costDiff,
            costDiffPercent = costDiffPercent,
            litersDiff = latest.liters - previous.liters,
            pricePerLiterDiff = latest.pricePerLiter - previous.pricePerLiter,
            previousTotal = previous.totalCost
        )
    }

    fun saveLog(onDone: () -> Unit) {
        if (!validateAndCheck()) return

        viewModelScope.launch {
            _addState.value = _addState.value.copy(isLoading = true)
            try {
                val state = _addState.value

                val vehicleId = if (state.selectedVehicleId != -1L)
                    state.selectedVehicleId
                else
                    vehicleRepository.getActive().first()?.id ?: return@launch

                val liters = state.liters.toDoubleOrNull()
                val price = state.pricePerLiter.toDoubleOrNull()
                val total = state.totalCost.toDoubleOrNull() ?: return@launch
                val km = state.km.toIntOrNull()

                fuelRepository.insert(
                    FuelLogEntity(
                        vehicleId = vehicleId,
                        date = state.date,
                        km = km,
                        liters = liters,
                        pricePerLiter = price,
                        totalCost = total,
                        station = state.station
                    )
                )
                // Verificar precio vs carga anterior (solo si tenemos precio de ambas)
                val previousPrice = uiState.value.fuelLogs.firstOrNull()?.pricePerLiter
                if (previousPrice != null && price != null && price > 0) {
                    SmartNotificationScheduler.checkFuelPriceIncrease(
                        context = context,
                        currentPrice = price,
                        previousPrice = previousPrice,
                        station = state.station.ifBlank { "la estación" }
                    )
                }
                // Solo empujamos el odómetro hacia adelante: si es una carga vieja que se
                // está cargando ahora con un km menor al actual, no queremos retrocederlo.
                val vehicleCurrentKm = uiState.value.vehicle?.currentKm ?: 0
                if (km != null && km > 0 && km >= vehicleCurrentKm) {
                    vehicleRepository.updateKm(vehicleId, km)
                    updateWidgets()
                }
                _addState.value = AddFuelUiState()
                onDone()
            } catch (e: Exception) {
                _addState.value = _addState.value.copy(
                    isLoading = false,
                    error = "Error al guardar la carga"
                )
            }
        }
    }

    private suspend fun updateWidgets() {
        try {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(LargeWidget::class.java).forEach { id ->
                LargeWidget().update(context, id)
            }
            manager.getGlanceIds(SmallWidget::class.java).forEach { id ->
                SmallWidget().update(context, id)
            }
        } catch (e: Exception) {
            android.util.Log.e("Widget", "Error updating widget: ${e.message}")
        }
    }
}
