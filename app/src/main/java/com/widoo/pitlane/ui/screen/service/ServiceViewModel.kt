package com.widoo.pitlane.ui.screen.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.data.repository.ServiceRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddServiceUiState(
    val selectedVehicleId: Long = -1L,     // ← nuevo
    val selectedVehicleName: String = "",   // ← nuevo
    val date: Long = System.currentTimeMillis(),
    val km: String = "",
    val serviceType: String = "",
    val oilBrand: String = "",
    val oilType: String = "",
    val oilViscosity: String = "",
    val filtersChanged: List<String> = emptyList(),
    val spareParts: List<SparePart> = emptyList(),
    val observations: String = "",
    val nextServiceKm: String = "",
    val cost: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

data class SparePart(
    val brand: String = "",
    val description: String = ""
)

class ServiceViewModel(
    private val serviceRepository: ServiceRepository,
    private val vehicleRepository: VehicleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val services: StateFlow<List<ServiceRecordEntity>> = vehicleRepository
        .getActive()
        .flatMapLatest { vehicle ->
            vehicle?.let { serviceRepository.getByVehicle(it.id) }
                ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicles: StateFlow<List<VehicleEntity>> = vehicleRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _addState = MutableStateFlow(AddServiceUiState())
    val addState: StateFlow<AddServiceUiState> = _addState.asStateFlow()

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

    fun onKmChange(v: String) { _addState.value = _addState.value.copy(km = v) }
    fun onServiceTypeChange(v: String) { _addState.value = _addState.value.copy(serviceType = v) }
    fun onOilBrandChange(v: String) { _addState.value = _addState.value.copy(oilBrand = v) }
    fun onOilTypeChange(v: String) { _addState.value = _addState.value.copy(oilType = v) }
    fun onOilViscosityChange(v: String) { _addState.value = _addState.value.copy(oilViscosity = v) }
    fun onObservationsChange(v: String) { _addState.value = _addState.value.copy(observations = v) }
    fun onNextServiceKmChange(v: String) { _addState.value = _addState.value.copy(nextServiceKm = v) }
    fun onCostChange(v: String) { _addState.value = _addState.value.copy(cost = v) }
    fun onDateChange(v: Long) { _addState.value = _addState.value.copy(date = v) }

    fun onFilterToggle(filter: String) {
        val current = _addState.value.filtersChanged.toMutableList()
        if (current.contains(filter)) current.remove(filter) else current.add(filter)
        _addState.value = _addState.value.copy(filtersChanged = current)
    }

    fun addSparePart() {
        val parts = _addState.value.spareParts.toMutableList()
        parts.add(SparePart())
        _addState.value = _addState.value.copy(spareParts = parts)
    }

    fun updateSparePart(index: Int, brand: String? = null, description: String? = null) {
        val parts = _addState.value.spareParts.toMutableList()
        parts[index] = parts[index].copy(
            brand = brand ?: parts[index].brand,
            description = description ?: parts[index].description
        )
        _addState.value = _addState.value.copy(spareParts = parts)
    }

    fun removeSparePart(index: Int) {
        val parts = _addState.value.spareParts.toMutableList()
        parts.removeAt(index)
        _addState.value = _addState.value.copy(spareParts = parts)
    }

    fun isFormValid(): Boolean {
        val s = _addState.value
        return s.km.isNotBlank() && s.serviceType.isNotBlank()
    }

    fun saveService(onDone: () -> Unit) {
        viewModelScope.launch {
            _addState.value = _addState.value.copy(isLoading = true)
            try {
                val s = _addState.value
                val vehicleId = if (s.selectedVehicleId != -1L)
                    s.selectedVehicleId
                else
                    vehicleRepository.getActive().first()?.id ?: return@launch

                val filtersJson = "[${s.filtersChanged.joinToString(",") { "\"$it\"" }}]"
                val partsJson = "[${s.spareParts.joinToString(",") {
                    "{\"marca\":\"${it.brand}\",\"desc\":\"${it.description}\"}"
                }}]"

                serviceRepository.insert(
                    ServiceRecordEntity(
                        vehicleId = vehicleId,
                        date = s.date,
                        km = s.km.replace(".", "").toIntOrNull() ?: 0,
                        serviceType = s.serviceType,
                        oilBrand = s.oilBrand,
                        oilType = s.oilType,
                        oilViscosity = s.oilViscosity,
                        filtersChanged = filtersJson,
                        spareParts = partsJson,
                        observations = s.observations,
                        nextServiceKm = s.nextServiceKm.replace(".", "").toIntOrNull() ?: 0,
                        cost = s.cost.replace(",", ".").toDoubleOrNull() ?: 0.0
                    )
                )
                _addState.value = AddServiceUiState()
                onDone()
            } catch (e: Exception) {
                _addState.value = _addState.value.copy(
                    isLoading = false,
                    error = "Error al guardar el servicio"
                )
            }
        }
    }
}