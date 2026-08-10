package com.widoo.pitlane.ui.screen.home

import android.content.Context
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
import com.widoo.pitlane.worker.SmartNotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val isLoading: Boolean = true,
    val vehicle: VehicleEntity? = null,
    val latestService: ServiceRecordEntity? = null,
    val services: List<ServiceRecordEntity> = emptyList(),
    val fuelLogs: List<FuelLogEntity> = emptyList(),
    val pendingReminders: List<ReminderEntity> = emptyList(),
    val serviceProgressPercent: Int = 0,
    val kmToNextService: Int = 0,
    // métricas motivadoras
    val daysSinceLastFuel: Int = 0,
    val daysSinceLastService: Int = 0,
    val fuelCostThisMonth: Double = 0.0,
    val fuelCostLastMonth: Double = 0.0,
    val avgConsumption: Double = 0.0,
    val totalServicesThisYear: Int = 0,
    val totalSpentThisYear: Double = 0.0,
    val consumptionTrend: Float = 0f  // positivo = mejoró, negativo = empeoró
)

class HomeViewModel(
    private val serviceRepository: ServiceRepository,
    private val fuelRepository: FuelRepository,
    private val reminderRepository: ReminderRepository,
    private val vehicleRepository: VehicleRepository,
    private val context: Context
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

        // Días desde última carga
        val daysSinceLastFuel = fuelLogs.firstOrNull()?.let {
            ((System.currentTimeMillis() - it.date) / (1000 * 60 * 60 * 24)).toInt()
        } ?: 0

        // Días desde último service
        val daysSinceLastService = latestService?.let {
            ((System.currentTimeMillis() - it.date) / (1000 * 60 * 60 * 24)).toInt()
        } ?: 0

        // Gasto combustible este mes y mes anterior
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val startOfMonth = cal.timeInMillis

        cal.add(Calendar.MONTH, -1)
        val startOfLastMonth = cal.timeInMillis

        val fuelCostThisMonth = fuelLogs.filter { it.date >= startOfMonth }
            .sumOf { it.totalCost }
        val fuelCostLastMonth = fuelLogs
            .filter { it.date >= startOfLastMonth && it.date < startOfMonth }
            .sumOf { it.totalCost }

        // Consumo promedio
        val avgConsumption = if (fuelLogs.size >= 2) {
            val sorted = fuelLogs.sortedBy { it.km }
            val kmDiff = (sorted.last().km - sorted.first().km).toDouble()
            val totalLiters = sorted.dropLast(1).sumOf { it.liters }
            if (kmDiff > 0) (totalLiters / kmDiff) * 100 else 0.0
        } else 0.0

        // Consumo trend — comparar últimas 2 cargas vs las 2 anteriores
        val consumptionTrend = if (fuelLogs.size >= 4) {
            val sorted = fuelLogs.sortedBy { it.km }
            val recentAvg = sorted.takeLast(2).map { it.liters }.average()
            val previousAvg = sorted.dropLast(2).takeLast(2).map { it.liters }.average()
            ((previousAvg - recentAvg) / previousAvg * 100).toFloat()
        } else 0f

        // Total año
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearCal = Calendar.getInstance()
        val servicesThisYear = services.filter {
            yearCal.timeInMillis = it.date
            yearCal.get(Calendar.YEAR) == currentYear
        }
        val totalSpentThisYear = servicesThisYear.sumOf { it.cost } +
                fuelLogs.filter {
                    yearCal.timeInMillis = it.date
                    yearCal.get(Calendar.YEAR) == currentYear
                }.sumOf { it.totalCost }

        HomeUiState(
            isLoading = false,
            vehicle = vehicle,
            latestService = latestService,
            services = services,
            fuelLogs = fuelLogs,
            pendingReminders = reminders,
            serviceProgressPercent = progressPercent,
            kmToNextService = kmToNext,
            daysSinceLastFuel = daysSinceLastFuel,
            daysSinceLastService = daysSinceLastService,
            fuelCostThisMonth = fuelCostThisMonth,
            fuelCostLastMonth = fuelCostLastMonth,
            avgConsumption = avgConsumption,
            totalServicesThisYear = servicesThisYear.size,
            totalSpentThisYear = totalSpentThisYear,
            consumptionTrend = consumptionTrend
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        HomeUiState(isLoading = true)
    )

    fun updateCurrentKm(km: Int) {
        viewModelScope.launch {
            val vehicle = uiState.value.vehicle ?: return@launch
            vehicleRepository.updateKm(vehicle.id, km)

            // Verificar si se acerca el próximo service
            val latestService = uiState.value.latestService
            if (latestService?.nextServiceKm != null && latestService.nextServiceKm > 0) {
                SmartNotificationScheduler.scheduleServiceKmCheck(
                    context = context,
                    currentKm = km,
                    nextServiceKm = latestService.nextServiceKm,
                    vehicleName = "${vehicle.brand} ${vehicle.model}"
                )
            }
        }
    }
}