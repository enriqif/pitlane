package com.widoo.pitlane.ui.screen.fuel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widoo.pitlane.data.local.entity.FuelLogEntity
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.ui.screen.profile.VehicleSelector
import com.widoo.pitlane.ui.theme.ElectricCyan
import com.widoo.pitlane.ui.theme.LocalAccentColor
import com.widoo.pitlane.ui.theme.SuccessGreen
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FuelScreen(viewModel: FuelViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val addState by viewModel.addState.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "GASTO ESTE MES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${NumberFormat.getNumberInstance(Locale("es", "AR"))
                            .format(state.monthlyTotal)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = LocalAccentColor.current
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Promedio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (state.avgConsumption > 0)
                                    "${String.format("%.1f", state.avgConsumption)} L/100km"
                                else "Sin datos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Última carga",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = state.fuelLogs.firstOrNull()?.let {
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(Date(it.date))
                                } ?: "Sin cargas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Comparison card
            state.comparison?.let { comparison ->
                ComparisonCard(comparison = comparison)
            }

            // List
            if (state.fuelLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⛽", style = MaterialTheme.typography.displayLarge)
                        Text(
                            text = "Sin cargas registradas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tocá + para agregar la primera",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Historial de cargas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 4.dp, bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.fuelLogs) { log ->

                        FuelLogCard(log = log)
                    }
                }
            }
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            containerColor = ElectricCyan,
            contentColor = Color.Black,
            icon = { Icon(Icons.Filled.LocalGasStation, contentDescription = null) },
            text = {
                Text(
                    "Cargar combustible",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        )
    }

    if (showAddSheet) {
        AddFuelSheet(
            viewModel = viewModel,
            vehicles = vehicles,
            onDismiss = { showAddSheet = false },
            onSaved = { showAddSheet = false }
        )
    }
}


@Composable
private fun ComparisonCard(comparison: FuelComparisonData) {
    val numFormat = NumberFormat.getNumberInstance(Locale("es", "AR"))
    val costUp = comparison.costDiff > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "VS CARGA ANTERIOR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cost comparison
                ComparisonItem(
                    modifier = Modifier.weight(1f),
                    label = "Costo total",
                    value = "${if (costUp) "+" else ""}$${
                        numFormat.format(comparison.costDiff)
                    }",
                    subValue = "${
                        if (costUp) "+" else ""
                    }${String.format("%.1f", comparison.costDiffPercent)}%",
                    isPositive = !costUp  // lower cost = positive
                )

                // Km diff
                ComparisonItem(
                    modifier = Modifier.weight(1f),
                    label = "Km recorridos",
                    value = "${numFormat.format(comparison.kmDiff)} km",
                    subValue = "desde última carga",
                    isNeutral = true
                )

                // Price per liter
                val priceUp = comparison.pricePerLiterDiff > 0
                ComparisonItem(
                    modifier = Modifier.weight(1f),
                    label = "Precio/L",
                    value = "${if (priceUp) "+" else ""}$${
                        numFormat.format(comparison.pricePerLiterDiff)
                    }",
                    subValue = "vs carga ant.",
                    isPositive = !priceUp
                )
            }
        }
    }
}

@Composable
private fun ComparisonItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subValue: String,
    isPositive: Boolean = true,
    isNeutral: Boolean = false
) {
    val valueColor = when {
        isNeutral -> MaterialTheme.colorScheme.onSurface
        isPositive -> SuccessGreen
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = subValue,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun FuelLogCard(log: FuelLogEntity) {
    val formatter = NumberFormat.getNumberInstance(Locale("es", "AR"))
    val date = SimpleDateFormat("dd MMM yyyy", Locale("es", "AR"))
        .format(Date(log.date))

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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        ElectricCyan.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LocalGasStation,
                    contentDescription = null,
                    tint = LocalAccentColor.current,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Left info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (log.station.isNotBlank()) log.station else "Estación",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = log.km?.let { "$date · ${formatter.format(it)} km" } ?: date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // ← Price per liter (si se cargó)
                log.pricePerLiter?.let {
                    Text(
                        text = "$${formatter.format(it)}/L",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalAccentColor.current,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right — total and liters
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${formatter.format(log.totalCost)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                log.liters?.let {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElectricCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${String.format("%.1f", it)} L",
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalAccentColor.current,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFuelSheet(
    viewModel: FuelViewModel,
    vehicles: List<VehicleEntity>,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.addState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Nueva carga",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            VehicleSelector(
                vehicles = vehicles,
                selectedVehicleName = state.selectedVehicleName,
                onVehicleSelected = viewModel::onVehicleSelected
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Total — el único campo obligatorio. Se autocompleta si cargás litros y
            // precio, pero también se puede escribir directo (útil para una carga vieja
            // de la que no te acordás el detalle exacto).
            OutlinedTextField(
                value = state.totalCost,
                onValueChange = viewModel::onTotalCostChange,
                label = { Text("Monto total") },
                prefix = { Text("$", color = LocalAccentColor.current) },
                isError = state.totalError != null,
                supportingText = {
                    Text(
                        text = state.totalError ?: "El único dato obligatorio",
                        color = if (state.totalError != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                colors = fuelTextFieldColors()
            )

            Text(
                text = "Detalles opcionales",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Km (opcional)
            OutlinedTextField(
                value = state.km,
                onValueChange = viewModel::onKmChange,
                label = { Text("Kilometraje") },
                suffix = { Text("km", color = LocalAccentColor.current) },
                isError = state.kmError != null,
                supportingText = state.kmError?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                colors = fuelTextFieldColors()
            )

            // Litros + Precio (opcionales)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.liters,
                    onValueChange = viewModel::onLitersChange,
                    label = { Text("Litros") },
                    suffix = { Text("L", color = LocalAccentColor.current) },
                    isError = state.litersError != null,
                    supportingText = state.litersError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    colors = fuelTextFieldColors()
                )
                OutlinedTextField(
                    value = state.pricePerLiter,
                    onValueChange = viewModel::onPricePerLiterChange,
                    label = { Text("Precio/L") },
                    prefix = { Text("$", color = LocalAccentColor.current) },
                    isError = state.priceError != null,
                    supportingText = state.priceError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    colors = fuelTextFieldColors()
                )
            }

            // Estación opcional
            OutlinedTextField(
                value = state.station,
                onValueChange = viewModel::onStationChange,
                label = { Text("Estación (opcional)") },
                placeholder = { Text("Shell, YPF, Axion...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                colors = fuelTextFieldColors()
            )

            // Save button — always at the bottom
            Button(
                onClick = { viewModel.saveLog(onSaved) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = viewModel.isFormValid() && !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Guardar carga",
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isFormValid()) Color.Black
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun fuelTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LocalAccentColor.current,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor = LocalAccentColor.current,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = LocalAccentColor.current
)