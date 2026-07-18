package com.widoo.pitlane.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.PreferencesManager
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 0,       // 0=welcome, 1=vehicle, 2=lastService, 3=notifications
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val plate: String = "",
    val currentKm: String = "",
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

class OnboardingViewModel(
    private val vehicleRepository: VehicleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onBrandChange(value: String) = _uiState.value.let { _uiState.value = it.copy(brand = value) }
    fun onModelChange(value: String) = _uiState.value.let { _uiState.value = it.copy(model = value) }
    fun onYearChange(value: String) = _uiState.value.let { _uiState.value = it.copy(year = value) }
    fun onPlateChange(value: String) = _uiState.value.let { _uiState.value = it.copy(plate = value.uppercase()) }
    fun onKmChange(value: String) = _uiState.value.let { _uiState.value = it.copy(currentKm = value) }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0)
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
    }

    fun isVehicleFormValid(): Boolean {
        val s = _uiState.value
        return s.brand.isNotBlank() && s.model.isNotBlank() &&
                s.year.isNotBlank() && s.plate.isNotBlank() &&
                s.currentKm.isNotBlank()
    }

    fun saveVehicleAndComplete(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val s = _uiState.value
                val vehicleId = vehicleRepository.insert(
                    VehicleEntity(
                        brand = s.brand.trim(),
                        model = s.model.trim(),
                        year = s.year.toIntOrNull() ?: 0,
                        plate = s.plate.trim(),
                        currentKm = s.currentKm.replace(".", "").toIntOrNull() ?: 0,
                        isActive = true
                    )
                )
                preferencesManager.setActiveVehicleId(vehicleId)
                preferencesManager.setOnboardingCompleted()
                onDone()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar el vehículo"
                )
            }
        }
    }

    fun onNotificationsToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }
}