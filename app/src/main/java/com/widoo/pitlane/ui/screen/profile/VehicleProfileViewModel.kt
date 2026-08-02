package com.widoo.pitlane.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.VehicleCatalog
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditVehicleUiState(
    val id: Long = 0,
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val plate: String = "",
    val currentKm: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

data class VehicleProfileUiState(
    val vehicles: List<VehicleEntity> = emptyList(),
    val activeVehicle: VehicleEntity? = null,
    val canAddMore: Boolean = true
)

class VehicleProfileViewModel(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleProfileUiState())
    val uiState: StateFlow<VehicleProfileUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow(EditVehicleUiState())
    val editState: StateFlow<EditVehicleUiState> = _editState.asStateFlow()

    init {
        viewModelScope.launch {
            vehicleRepository.getAll().collect { vehicles ->
                _uiState.value = VehicleProfileUiState(
                    vehicles = vehicles,
                    activeVehicle = vehicles.firstOrNull { it.isActive },
                    canAddMore = vehicles.size < 3
                )
            }
        }
    }

    fun loadVehicleForEdit(vehicle: VehicleEntity) {
        _editState.value = EditVehicleUiState(
            id = vehicle.id,
            brand = vehicle.brand,
            model = vehicle.model,
            year = vehicle.year.toString(),
            plate = vehicle.plate,
            currentKm = vehicle.currentKm.toString()
        )
    }

    fun onBrandChange(v: String) {
        _editState.value = _editState.value.copy(brand = v, model = "")
    }
    fun onModelChange(v: String) {
        _editState.value = _editState.value.copy(model = v)
    }
    fun onYearChange(v: String) {
        _editState.value = _editState.value.copy(year = v)
    }
    fun onPlateChange(v: String) {
        _editState.value = _editState.value.copy(plate = v.uppercase())
    }
    fun onKmChange(v: String) {
        _editState.value = _editState.value.copy(currentKm = v)
    }

    fun isEditFormValid(): Boolean {
        val s = _editState.value
        return s.brand.isNotBlank() && s.model.isNotBlank() &&
                s.year.isNotBlank() && s.plate.isNotBlank() &&
                s.currentKm.isNotBlank()
    }

    fun saveEdit(onDone: () -> Unit) {
        viewModelScope.launch {
            _editState.value = _editState.value.copy(isLoading = true)
            try {
                val s = _editState.value
                vehicleRepository.update(
                    VehicleEntity(
                        id = s.id,
                        brand = s.brand.trim(),
                        model = s.model.trim(),
                        year = s.year.toIntOrNull() ?: 0,
                        plate = s.plate.trim(),
                        currentKm = s.currentKm.toIntOrNull() ?: 0,
                        isActive = uiState.value.activeVehicle?.id == s.id
                    )
                )
                _editState.value = EditVehicleUiState()
                onDone()
            } catch (e: Exception) {
                _editState.value = _editState.value.copy(
                    isLoading = false,
                    error = "Error al guardar los cambios"
                )
            }
        }
    }

    fun setActive(vehicle: VehicleEntity) {
        viewModelScope.launch {
            vehicleRepository.setActive(vehicle.id)
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            vehicleRepository.delete(vehicle)
        }
    }

    fun addNewVehicle(
        brand: String,
        model: String,
        year: Int,
        plate: String,
        km: Int
    ) {
        viewModelScope.launch {
            vehicleRepository.insert(
                VehicleEntity(
                    brand = brand,
                    model = model,
                    year = year,
                    plate = plate,
                    currentKm = km,
                    isActive = false
                )
            )
        }
    }
}