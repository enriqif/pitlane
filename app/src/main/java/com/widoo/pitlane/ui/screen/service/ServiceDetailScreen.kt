package com.widoo.pitlane.ui.screen.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.widoo.pitlane.data.local.entity.ServiceRecordEntity
import com.widoo.pitlane.ui.theme.ElectricCyan
import com.widoo.pitlane.ui.theme.PitlaneTheme
import org.json.JSONArray
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.collectAsState

@Composable
fun ServiceDetailScreen(
    serviceId: Long,
    navController: NavController,
    viewModel: ServiceViewModel = koinViewModel()
) {
    val services = viewModel.services.collectAsState().value
    val service = services.firstOrNull { it.id == serviceId }

    service?.let {
        ServiceDetailContent(
            service = it,
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
fun ServiceDetailContent(
    service: ServiceRecordEntity,
    onBack: () -> Unit
) {
    val numFormat = NumberFormat.getNumberInstance(Locale("es", "AR"))
    val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "AR"))

    // Parse filters
    val filters = try {
        val arr = JSONArray(service.filtersChanged)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) { emptyList() }

    // Parse spare parts
    data class SparePart(val brand: String, val description: String)
    val spareParts = try {
        val arr = JSONArray(service.spareParts)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            SparePart(
                brand = obj.optString("marca", ""),
                description = obj.optString("desc", "")
            )
        }
    } catch (e: Exception) { emptyList() }

    val accentColor = when {
        service.serviceType.contains("aceite", ignoreCase = true) -> ElectricCyan
        service.serviceType.contains("cubierta", ignoreCase = true) ||
                service.serviceType.contains("neumático", ignoreCase = true) -> Color(0xFF4FC3F7)
        service.serviceType.contains("freno", ignoreCase = true) -> Color(0xFFFF7043)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Detalle del Servicio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Build,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (service.cost > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$${numFormat.format(service.cost)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = service.serviceType,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailChip(
                            icon = Icons.Filled.CalendarToday,
                            text = dateFormat.format(Date(service.date))
                        )
                        DetailChip(
                            icon = Icons.Filled.Speed,
                            text = "${numFormat.format(service.km)} km"
                        )
                    }
                }
            }

            // Aceite
            if (service.oilBrand.isNotBlank()) {
                DetailSection(title = "Aceite") {
                    DetailRow(label = "Marca", value = service.oilBrand)
                    if (service.oilType.isNotBlank())
                        DetailRow(label = "Tipo", value = service.oilType)
                    if (service.oilViscosity.isNotBlank())
                        DetailRow(label = "Viscosidad", value = service.oilViscosity)
                }
            }

            // Filtros
            if (filters.isNotEmpty()) {
                DetailSection(title = "Filtros cambiados") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filters.forEach { filter ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElectricCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Medium,
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

            // Repuestos
            if (spareParts.isNotEmpty()) {
                DetailSection(title = "Repuestos utilizados") {
                    spareParts.forEach { part ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ElectricCyan)
                                )
                                Text(
                                    text = part.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (part.brand.isNotBlank()) {
                                Text(
                                    text = part.brand,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (spareParts.last() != part) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            // Próximo service
            if (service.nextServiceKm > 0 || service.nextServiceDate > 0) {
                DetailSection(title = "Próximo servicio") {
                    if (service.nextServiceKm > 0)
                        DetailRow(
                            label = "Kilometraje",
                            value = "${numFormat.format(service.nextServiceKm)} km",
                            valueColor = ElectricCyan
                        )
                    if (service.nextServiceDate > 0)
                        DetailRow(
                            label = "Fecha estimada",
                            value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(Date(service.nextServiceDate)),
                            valueColor = ElectricCyan
                        )
                }
            }

            // Observaciones
            if (service.observations.isNotBlank()) {
                DetailSection(title = "Observaciones") {
                    Text(
                        text = service.observations,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor == Color.Unspecified)
                MaterialTheme.colorScheme.onSurface
            else valueColor
        )
    }
}

@Composable
private fun DetailChip(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Service Detail")
@Composable
private fun ServiceDetailPreview() {
    PitlaneTheme {
        ServiceDetailContent(
            service = ServiceRecordEntity(
                id = 1,
                vehicleId = 1,
                date = System.currentTimeMillis(),
                km = 75000,
                serviceType = "Cambio de Aceite y Filtros",
                oilBrand = "Shell Helix",
                oilType = "Sintético",
                oilViscosity = "10W40",
                filtersChanged = "[\"Aceite\",\"Aire\",\"Habitáculo\"]",
                spareParts = "[{\"marca\":\"NGK\",\"desc\":\"Bujías x4\"},{\"marca\":\"Mann\",\"desc\":\"Filtro aceite\"}]",
                observations = "Se detectó leve pérdida de aceite en tapa de válvulas. Monitorear en próximo service.",
                nextServiceKm = 85000,
                cost = 45000.0
            ),
            onBack = {}
        )
    }
}