package com.widoo.pitlane.ui.screen.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.ui.theme.ElectricCyan
import com.widoo.pitlane.ui.theme.PitlaneTheme
import com.widoo.pitlane.ui.theme.PrimaryGreen
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ChartsScreen(viewModel: ChartsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    ChartsScreenContent(state = state)
}

@Composable
fun ChartsScreenContent(state: ChartsUiState) {
    val numFormat = NumberFormat.getNumberInstance(Locale("es", "AR"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            text = "Panel de Analíticas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Resumen de gastos y desempeño del vehículo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "GASTO TOTAL ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}",
                value = "$${numFormat.format(state.totalYearSpend)}",
                icon = "💰"
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                label = "PROMEDIO KM/SERVICIO",
                value = if (state.avgKmBetweenServices > 0)
                    "${numFormat.format(state.avgKmBetweenServices.toInt())} km"
                else "Sin datos",
                icon = "🔧"
            )
        }

        if (state.avgConsumption > 0) {
            SummaryCard(
                modifier = Modifier.fillMaxWidth(),
                label = "CONSUMO PROMEDIO",
                value = "${String.format("%.1f", state.avgConsumption)} L/100km",
                icon = "⛽"
            )
        }

        // Monthly costs chart
        if (state.monthlyData.any { it.fuelCost > 0 || it.serviceCost > 0 }) {
            ChartCard(title = "Costos Mensuales") {
                MonthlyCostChart(monthlyData = state.monthlyData)
            }
        }

        // Km between services chart
        if (state.services.size >= 2) {
            ChartCard(title = "Kilometraje entre Servicios") {
                KmBetweenServicesChart(services = state.services)
            }
        }

        // Fuel consumption chart
        if (state.fuelLogs.size >= 2) {
            ChartCard(title = "Consumo (L/100km)") {
                FuelConsumptionChart(fuelLogs = state.fuelLogs)
            }
        }

        // Empty state
        if (state.services.isEmpty() && state.fuelLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📊", fontSize = 64.sp)
                    Text(
                        text = "Sin datos para mostrar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Registrá servicios y cargas de combustible\npara ver tus estadísticas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 20.sp)
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Legend for monthly chart
            if (title == "Costos Mensuales") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    LegendItem(color = ElectricCyan, label = "Combustible")
                    LegendItem(color = PrimaryGreen, label = "Servicios")
                }
            }

            content()
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthlyCostChart(monthlyData: List<MonthlyData>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val months = monthlyData.map { it.month }
    val textColor = MaterialTheme.colorScheme.onSurface
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) -1 // 0-indexed

    LaunchedEffect(monthlyData) {
        modelProducer.runTransaction {
            columnSeries {
                series(monthlyData.map { it.fuelCost })
                series(monthlyData.map { it.serviceCost })
            }
        }
    }

    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.x(currentMonth.toFloat().toDouble())
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(color = textColor)
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(color = textColor),
                valueFormatter = { _, x, _ ->
                    months.getOrElse(x.toInt()) { "" }
                }
            )
        ),
        modelProducer = modelProducer,
        scrollState = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun KmBetweenServicesChart(services: List<ServiceRecordEntity>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val sorted = remember(services) { services.sortedBy { it.km } }

    val kmValues = sorted.map { it.km.toFloat() }

    androidx.compose.runtime.LaunchedEffect(services) {
        modelProducer.runTransaction {
            lineSeries { series(kmValues) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, x, _ -> "S${(x.toInt() + 1)}" }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@Composable
private fun FuelConsumptionChart(fuelLogs: List<FuelLogEntity>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val sorted = remember(fuelLogs) { fuelLogs.sortedBy { it.date } }

    // Calculate rolling consumption per fill-up
    val consumptions = remember(fuelLogs) {
        sorted.zipWithNext { a, b ->
            val kmDiff = (b.km - a.km).toFloat()
            if (kmDiff > 0) (a.liters.toFloat() / kmDiff) * 100f else 0f
        }.filter { it > 0 }
    }

    androidx.compose.runtime.LaunchedEffect(fuelLogs) {
        if (consumptions.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries { series(consumptions) }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom()
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

// Previews
@Preview(showBackground = true, showSystemUi = true, name = "Charts — Con datos")
@Composable
private fun ChartsScreenPreviewWithData() {
    PitlaneTheme {
        ChartsScreenContent(
            state = ChartsUiState(
                totalYearSpend = 185000.0,
                avgKmBetweenServices = 10000.0,
                avgConsumption = 9.5,
                monthlyData = listOf(
                    MonthlyData("Ene", 15000f, 0f),
                    MonthlyData("Feb", 18000f, 42000f),
                    MonthlyData("Mar", 22000f, 0f),
                    MonthlyData("Abr", 17000f, 15000f),
                    MonthlyData("May", 20000f, 0f),
                    MonthlyData("Jun", 19000f, 0f),
                    MonthlyData("Jul", 24000f, 8000f),
                    MonthlyData("Ago", 0f, 0f),
                    MonthlyData("Sep", 0f, 0f),
                    MonthlyData("Oct", 0f, 0f),
                    MonthlyData("Nov", 0f, 0f),
                    MonthlyData("Dic", 0f, 0f),
                )
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Charts — Sin datos")
@Composable
private fun ChartsScreenPreviewEmpty() {
    PitlaneTheme {
        ChartsScreenContent(state = ChartsUiState())
    }
}