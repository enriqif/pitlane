package com.widoo.pitlane.ui.screen.fuel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.VehicleRepository
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
    val priceError: String? = null
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
        _addState.value = _addState.value.copy(totalCost = v)
    }

    private fun recalculateTotal() {
        val liters = _addState.value.liters.toDoubleOrNull() ?: return
        val price = _addState.value.pricePerLiter.toDoubleOrNull() ?: return
        val total = liters * price
        _addState.value = _addState.value.copy(
            totalCost = String.format("%.2f", total)
        )
    }

    fun validateAndCheck(): Boolean {
        val s = _addState.value
        val vehicle = uiState.value.vehicle
        val lastLog = uiState.value.fuelLogs.firstOrNull()
        var hasError = false

        // Km validation
        val km = s.km.toIntOrNull()
        when {
            km == null || km <= 0 -> {
                _addState.value = _addState.value.copy(
                    kmError = "Ingresá un kilometraje válido"
                )
                hasError = true
            }
            vehicle != null && km < vehicle.currentKm -> {
                _addState.value = _addState.value.copy(
                    kmError = "El km debe ser mayor al actual (${vehicle.currentKm} km)"
                )
                hasError = true
            }
            lastLog != null && km < lastLog.km -> {
                _addState.value = _addState.value.copy(
                    kmError = "El km debe ser mayor a la última carga (${lastLog.km} km)"
                )
                hasError = true
            }
        }

        // Liters validation
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

        // Price validation
        val price = s.pricePerLiter.toDoubleOrNull()
        if (price == null || price <= 0) {
            _addState.value = _addState.value.copy(
                priceError = "Ingresá un precio válido"
            )
            hasError = true
        }

        return !hasError
    }

    fun isFormValid(): Boolean {
        val s = _addState.value
        return s.km.isNotBlank() &&
                s.liters.isNotBlank() &&
                s.pricePerLiter.isNotBlank() &&
                s.kmError == null &&
                s.litersError == null &&
                s.priceError == null
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
        if (logs.size < 2) return 0.0
        val sorted = logs.sortedBy { it.km }
        val kmDiff = (sorted.last().km - sorted.first().km).toDouble()
        val totalLiters = sorted.dropLast(1).sumOf { it.liters }
        if (kmDiff <= 0) return 0.0
        return (totalLiters / kmDiff) * 100
    }

    private fun buildComparison(logs: List<FuelLogEntity>): FuelComparisonData? {
        if (logs.size < 2) return null
        val latest = logs[0]
        val previous = logs[1]

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
                    preferencesManager.activeVehicleId.first()

                val liters = state.liters.toDoubleOrNull() ?: 0.0
                val price = state.pricePerLiter.toDoubleOrNull() ?: 0.0
                val total = state.totalCost.toDoubleOrNull() ?: (liters * price)
                val km = state.km.toIntOrNull() ?: 0

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
                // Verificar precio vs carga anterior
                val previousLog = uiState.value.fuelLogs.firstOrNull()
                if (previousLog != null && price > 0) {
                    SmartNotificationScheduler.checkFuelPriceIncrease(
                        context = context,
                        currentPrice = price,
                        previousPrice = previousLog.pricePerLiter,
                        station = state.station.ifBlank { "la estación" }
                    )
                }
                if (km > 0) vehicleRepository.updateKm(vehicleId, km)
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
}