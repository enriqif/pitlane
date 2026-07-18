package com.widoo.pitlane.ui.screen.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddFuelUiState(
    val date: Long = System.currentTimeMillis(),
    val km: String = "",
    val liters: String = "",
    val pricePerLiter: String = "",
    val totalCost: String = "",
    val station: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class FuelViewModel(
    private val fuelRepository: FuelRepository,
    private val vehicleRepository: VehicleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val fuelLogs: StateFlow<List<FuelLogEntity>> = fuelRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _addState = MutableStateFlow(AddFuelUiState())
    val addState: StateFlow<AddFuelUiState> = _addState.asStateFlow()

    fun onKmChange(v: String) { _addState.value = _addState.value.copy(km = v) }
    fun onStationChange(v: String) { _addState.value = _addState.value.copy(station = v) }

    fun onLitersChange(v: String) {
        _addState.value = _addState.value.copy(liters = v)
        recalculateTotal()
    }

    fun onPricePerLiterChange(v: String) {
        _addState.value = _addState.value.copy(pricePerLiter = v)
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

    fun isFormValid(): Boolean {
        val s = _addState.value
        return s.km.isNotBlank() && s.liters.isNotBlank() && s.pricePerLiter.isNotBlank()
    }

    fun getMonthlyTotal(): Double {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val startOfMonth = cal.timeInMillis
        return fuelLogs.value
            .filter { it.date >= startOfMonth }
            .sumOf { it.totalCost }
    }

    fun getAverageConsumption(): Double {
        val logs = fuelLogs.value
        if (logs.size < 2) return 0.0
        val sorted = logs.sortedBy { it.km }
        val kmDiff = (sorted.last().km - sorted.first().km).toDouble()
        val totalLiters = sorted.dropLast(1).sumOf { it.liters }
        if (kmDiff <= 0) return 0.0
        return (totalLiters / kmDiff) * 100
    }

    fun saveLog(onDone: () -> Unit) {
        viewModelScope.launch {
            _addState.value = _addState.value.copy(isLoading = true)
            try {
                val vehicleId = preferencesManager.activeVehicleId.first()
                val s = _addState.value
                val liters = s.liters.toDoubleOrNull() ?: 0.0
                val price = s.pricePerLiter.toDoubleOrNull() ?: 0.0
                val total = s.totalCost.toDoubleOrNull() ?: (liters * price)
                val km = s.km.toIntOrNull() ?: 0

                fuelRepository.insert(
                    FuelLogEntity(
                        vehicleId = vehicleId,
                        date = s.date,
                        km = km,
                        liters = liters,
                        pricePerLiter = price,
                        totalCost = total,
                        station = s.station
                    )
                )
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