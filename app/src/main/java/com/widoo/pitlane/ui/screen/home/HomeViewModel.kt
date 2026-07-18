package com.widoo.pitlane.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.ReminderEntity
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.ReminderRepository
import com.widoo.pitlane.data.repository.ServiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    serviceRepository: ServiceRepository,
    fuelRepository: FuelRepository,
    reminderRepository: ReminderRepository
) : ViewModel() {

    val services: StateFlow<List<ServiceRecordEntity>> = serviceRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuelLogs: StateFlow<List<FuelLogEntity>> = fuelRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReminders: StateFlow<List<ReminderEntity>> = reminderRepository
        .getPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}