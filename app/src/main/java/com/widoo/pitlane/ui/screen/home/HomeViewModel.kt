package com.widoo.pitlane.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.ReminderEntity
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.ReminderRepository
import com.widoo.pitlane.data.repository.ServiceRepository
import com.widoo.pitlane.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val vehicle: VehicleEntity? = null,
    val latestService: ServiceRecordEntity? = null,
    val services: List<ServiceRecordEntity> = emptyList(),
    val fuelLogs: List<FuelLogEntity> = emptyList(),
    val pendingReminders: List<ReminderEntity> = emptyList(),
    val serviceProgressPercent: Int = 0,
    val kmToNextService: Int = 0
)

class HomeViewModel(
    private val serviceRepository: ServiceRepository,
    private val fuelRepository: FuelRepository,
    private val reminderRepository: ReminderRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        vehicleRepository.getActive(),
        serviceRepository.getAll(),
        fuelRepository.getAll(),
        reminderRepository.getPending()
    ) { vehicle, services, fuelLogs, reminders ->

        val latestService = services.firstOrNull()
        val currentKm = vehicle?.currentKm ?: 0
        val lastServiceKm = latestService?.km ?: 0
        val nextServiceKm = latestService?.nextServiceKm ?: 0

        val progressPercent = if (nextServiceKm > lastServiceKm) {
            val done = currentKm - lastServiceKm
            val total = nextServiceKm - lastServiceKm
            ((done.toFloat() / total.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0

        val kmToNext = if (nextServiceKm > currentKm) nextServiceKm - currentKm else 0

        HomeUiState(
            vehicle = vehicle,
            latestService = latestService,
            services = services,
            fuelLogs = fuelLogs,
            pendingReminders = reminders,
            serviceProgressPercent = progressPercent,
            kmToNextService = kmToNext
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState()
    )
}