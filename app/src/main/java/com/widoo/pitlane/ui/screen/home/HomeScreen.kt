package com.widoo.pitlane.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widoo.pitlane.ui.theme.ElectricCyan
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val numFormat = NumberFormat.getNumberInstance(Locale("es", "AR"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Pitlane",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            }
        }

        // Vehicle card
        state.vehicle?.let { vehicle ->
            var showEditKmDialog by remember { mutableStateOf(false) }
            var kmInput by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "VEHÍCULO ACTIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${numFormat.format(vehicle.currentKm)} km",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            // Botón editar km
                            IconButton(
                                onClick = {
                                    kmInput = vehicle.currentKm.toString()
                                    showEditKmDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Editar km",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ElectricCyan.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "● ${vehicle.plate}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }
                    state.latestService?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Último service: ${
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(Date(it.date))
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Dialog editar km
            if (showEditKmDialog) {
                AlertDialog(
                    onDismissRequest = { showEditKmDialog = false },
                    title = {
                        Text(
                            text = "Actualizar kilometraje",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Ingresá el kilometraje actual de tu vehículo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = kmInput,
                                onValueChange = { if (it.length <= 7) kmInput = it },
                                label = { Text("Kilometraje") },
                                suffix = { Text("km", color = ElectricCyan) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedLabelColor = ElectricCyan,
                                    cursorColor = ElectricCyan
                                )
                            )
                            // Validación
                            val kmValue = kmInput.toIntOrNull() ?: 0
                            if (kmValue > 0) {
                                Text(
                                    text = "El kilometraje no puede ser 0)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    confirmButton = {
                        val kmValue = kmInput.toIntOrNull() ?: 0
                        Button(
                            onClick = {
                                if (kmValue >= vehicle.currentKm) {
                                    viewModel.updateCurrentKm(kmValue)
                                    showEditKmDialog = false
                                }
                            },
                            enabled = kmInput.isNotBlank() &&
                                    (kmInput.toIntOrNull() ?: 0) >= vehicle.currentKm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditKmDialog = false }) {
                            Text("Cancelar")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        } ?: run {
            // Empty vehicle state
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin vehículo registrado",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Alert card for pending reminders
        if (state.pendingReminders.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alerta de Trámite",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = state.pendingReminders.first().title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.services.size}",
                label = "Servicios",
                icon = "🔧"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.fuelLogs.size}",
                label = "Cargas",
                icon = "⛽"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.pendingReminders.size}",
                label = "Alertas",
                icon = "🔔",
                valueColor = if (state.pendingReminders.isNotEmpty())
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )

            StatsSection(state = state, numFormat = numFormat)
        }

        // Last service card with progress
        state.latestService?.let { service ->
            Text(
                text = "Último Servicio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ElectricCyan.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = service.serviceType,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                                .format(Date(service.date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (service.oilBrand.isNotBlank()) {
                        Text(
                            text = buildString {
                                append(service.oilBrand)
                                if (service.oilViscosity.isNotBlank())
                                    append(" ${service.oilViscosity}")
                                if (service.oilType.isNotBlank())
                                    append(" · ${service.oilType}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "KILOMETRAJE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${numFormat.format(service.km)} km",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (service.nextServiceKm > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "PRÓXIMO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${numFormat.format(service.nextServiceKm)} km",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }

                    if (service.nextServiceKm > 0 && state.serviceProgressPercent > 0) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PROGRESO AL PRÓXIMO SERVICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${state.serviceProgressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { state.serviceProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricCyan,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        if (state.kmToNextService > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Faltan ${numFormat.format(state.kmToNextService)} km para el próximo service",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Pending reminders list
        if (state.pendingReminders.isNotEmpty()) {
            Text(
                text = "Recordatorios Pendientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            state.pendingReminders.take(3).forEach { reminder ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Vence: ${
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(Date(reminder.triggerDate))
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSection(
    state: HomeUiState,
    numFormat: NumberFormat
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = "Resumen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Gasto este mes vs mes anterior
        if (state.fuelCostThisMonth > 0 || state.fuelCostLastMonth > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "COMBUSTIBLE ESTE MES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$${numFormat.format(state.fuelCostThisMonth)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    if (state.fuelCostLastMonth > 0) {
                        val diff = state.fuelCostThisMonth - state.fuelCostLastMonth
                        val diffPercent = (diff / state.fuelCostLastMonth) * 100
                        val isUp = diff > 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isUp) "▲" else "▼",
                                color = if (isUp) MaterialTheme.colorScheme.error
                                else Color(0xFF4CAF50),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${String.format("%.1f", Math.abs(diffPercent))}% " +
                                        "vs mes anterior",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isUp) MaterialTheme.colorScheme.error
                                else Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        }

        // Consumo promedio con trend
        if (state.avgConsumption > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONSUMO PROMEDIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${String.format("%.1f", state.avgConsumption)} L/100km",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (state.consumptionTrend != 0f) {
                        val improved = state.consumptionTrend > 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (improved) Color(0xFF4CAF50).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (improved) "↑ Mejoró" else "↓ Empeoró",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (improved) Color(0xFF4CAF50)
                                else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Días desde última carga
        if (state.daysSinceLastFuel > 0) {
            InsightCard(
                emoji = "⛽",
                text = when {
                    state.daysSinceLastFuel == 1 -> "Cargaste nafta ayer"
                    state.daysSinceLastFuel < 7 -> "Hace ${state.daysSinceLastFuel} días que no cargás nafta"
                    state.daysSinceLastFuel < 30 -> "Hace ${state.daysSinceLastFuel} días sin cargar — ¿cómo va el tanque?"
                    else -> "Hace más de un mes sin registrar una carga"
                }
            )
        }

        // Días desde último service
        if (state.daysSinceLastService > 0) {
            InsightCard(
                emoji = "🔧",
                text = when {
                    state.daysSinceLastService < 30 -> "Último service hace ${state.daysSinceLastService} días — todo en orden"
                    state.daysSinceLastService < 90 -> "Hace ${state.daysSinceLastService} días del último service"
                    state.daysSinceLastService < 180 -> "Hace ${state.daysSinceLastService} días sin service — ¿cómo va el aceite?"
                    else -> "Más de 6 meses sin service — ¡es momento de revisarlo!"
                },
                isWarning = state.daysSinceLastService >= 180
            )
        }

        // Total gastado este año
        if (state.totalSpentThisYear > 0) {
            InsightCard(
                emoji = "💰",
                text = "Gastaste $${numFormat.format(state.totalSpentThisYear)} en tu auto este año"
            )
        }
    }
}

@Composable
private fun InsightCard(
    emoji: String,
    text: String,
    isWarning: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isWarning)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: String,
    valueColor: Color = Color.Unspecified
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (valueColor == Color.Unspecified)
                    MaterialTheme.colorScheme.onSurface
                else valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}