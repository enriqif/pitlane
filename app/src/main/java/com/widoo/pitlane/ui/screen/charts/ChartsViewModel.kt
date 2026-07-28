package com.widoo.pitlane.ui.screen.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.data.repository.FuelRepository
import com.widoo.pitlane.data.repository.ServiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class MonthlyData(
    val month: String,
    val fuelCost: Float,
    val serviceCost: Float
)

data class ChartsUiState(
    val services: List<ServiceRecordEntity> = emptyList(),
    val fuelLogs: List<FuelLogEntity> = emptyList(),
    val monthlyData: List<MonthlyData> = emptyList(),
    val totalYearSpend: Double = 0.0,
    val avgKmBetweenServices: Double = 0.0,
    val avgConsumption: Double = 0.0
)

class ChartsViewModel(
    private val serviceRepository: ServiceRepository,
    private val fuelRepository: FuelRepository
) : ViewModel() {

    val uiState: StateFlow<ChartsUiState> = combine(
        serviceRepository.getAll(),
        fuelRepository.getAll()
    ) { services, fuelLogs ->

        val monthlyData = buildMonthlyData(services, fuelLogs)
        val totalYear = calculateYearTotal(services, fuelLogs)
        val avgKm = calculateAvgKmBetweenServices(services)
        val avgConsumption = calculateAvgConsumption(fuelLogs)

        ChartsUiState(
            services = services,
            fuelLogs = fuelLogs,
            monthlyData = monthlyData,
            totalYearSpend = totalYear,
            avgKmBetweenServices = avgKm,
            avgConsumption = avgConsumption
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChartsUiState()
    )

    private fun buildMonthlyData(
        services: List<ServiceRecordEntity>,
        fuelLogs: List<FuelLogEntity>
    ): List<MonthlyData> {
        val months = listOf(
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
        )
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        return months.mapIndexed { index, month ->
            val fuelCost = fuelLogs.filter { log ->
                val cal = Calendar.getInstance()  // ← new instance per item
                cal.timeInMillis = log.date
                cal.get(Calendar.MONTH) == index &&
                        cal.get(Calendar.YEAR) == currentYear
            }.sumOf { it.totalCost }.toFloat()

            val serviceCost = services.filter { service ->
                val cal = Calendar.getInstance()  // ← new instance per item
                cal.timeInMillis = service.date
                cal.get(Calendar.MONTH) == index &&
                        cal.get(Calendar.YEAR) == currentYear
            }.sumOf { it.cost }.toFloat()

            MonthlyData(month, fuelCost, serviceCost)
        }
    }

    private fun calculateYearTotal(
        services: List<ServiceRecordEntity>,
        fuelLogs: List<FuelLogEntity>
    ): Double {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        val fuelTotal = fuelLogs.filter { log ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = log.date
            cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.totalCost }

        val serviceTotal = services.filter { service ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = service.date
            cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.cost }

        return fuelTotal + serviceTotal
    }

    private fun calculateAvgKmBetweenServices(
        services: List<ServiceRecordEntity>
    ): Double {
        if (services.size < 2) return 0.0
        val sorted = services.sortedBy { it.km }
        val diffs = sorted.zipWithNext { a, b -> (b.km - a.km).toDouble() }
        return diffs.average()
    }

    private fun calculateAvgConsumption(fuelLogs: List<FuelLogEntity>): Double {
        if (fuelLogs.size < 2) return 0.0
        val sorted = fuelLogs.sortedBy { it.km }
        val kmDiff = (sorted.last().km - sorted.first().km).toDouble()
        val totalLiters = sorted.dropLast(1).sumOf { it.liters }
        if (kmDiff <= 0) return 0.0
        return (totalLiters / kmDiff) * 100
    }
}