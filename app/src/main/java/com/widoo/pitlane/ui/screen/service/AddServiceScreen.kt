package com.widoo.pitlane.ui.screen.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.widoo.pitlane.ui.theme.ElectricCyan
import org.koin.androidx.compose.koinViewModel

val SERVICE_TYPES = listOf(
    "Cambio de aceite y filtros",
    "Cambio de aceite",
    "Filtro de aire",
    "Filtro de combustible",
    "Filtro de habitáculo",
    "Rotación de neumáticos",
    "Alineación y balanceo",
    "Pastillas de freno",
    "Discos de freno",
    "Correa de distribución",
    "Bujías",
    "Líquido de frenos",
    "Líquido de refrigerante",
    "Batería",
    "Amortiguadores",
    "Revisión general / Pre-VTV",
    "Otro"
)

val OIL_TYPES = listOf("Mineral", "Semi-sintético", "Sintético", "Sintético total")
val OIL_VISCOSITIES = listOf("5W30", "5W40", "10W40", "15W40", "20W50", "0W20")
val FILTERS = listOf("Aceite", "Aire", "Combustible", "Habitáculo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    navController: NavController,
    viewModel: ServiceViewModel = koinViewModel()
) {
    val state by viewModel.addState.collectAsState()
    var serviceTypeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Nuevo Servicio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // SECCIÓN 1 — General
            SectionTitle("General")

            // Km actual
            PitlaneTextField(
                value = state.km,
                onValueChange = viewModel::onKmChange,
                label = "Kilometraje actual",
                placeholder = "87450",
                suffix = "km",
                keyboardType = KeyboardType.Number
            )

            // Tipo de servicio
            ExposedDropdownMenuBox(
                expanded = serviceTypeExpanded,
                onExpandedChange = { serviceTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.serviceType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de servicio") },
                    placeholder = { Text("Seleccioná el servicio") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceTypeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(8.dp),
                    colors = pitlaneTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = serviceTypeExpanded,
                    onDismissRequest = { serviceTypeExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    SERVICE_TYPES.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.onServiceTypeChange(type)
                                serviceTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // SECCIÓN 2 — Aceite
            SectionTitle("Aceite")

            PitlaneTextField(
                value = state.oilBrand,
                onValueChange = viewModel::onOilBrandChange,
                label = "Marca del aceite",
                placeholder = "Ej: Mobil, Shell, YPF",
                capitalization = KeyboardCapitalization.Words
            )

            // Tipo de aceite chips
            ChipSelector(
                label = "Tipo",
                options = OIL_TYPES,
                selected = state.oilType,
                onSelect = viewModel::onOilTypeChange
            )

            // Viscosidad chips
            ChipSelector(
                label = "Viscosidad",
                options = OIL_VISCOSITIES,
                selected = state.oilViscosity,
                onSelect = viewModel::onOilViscosityChange
            )

            // SECCIÓN 3 — Filtros
            SectionTitle("Filtros cambiados")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FILTERS.forEach { filter ->
                    val selected = state.filtersChanged.contains(filter)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onFilterToggle(filter) },
                        label = { Text(filter, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = ElectricCyan,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // SECCIÓN 4 — Repuestos
            SectionTitle("Repuestos utilizados")

            state.spareParts.forEachIndexed { index, part ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = part.brand,
                        onValueChange = { viewModel.updateSparePart(index, brand = it) },
                        label = { Text("Marca") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = pitlaneTextFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = part.description,
                        onValueChange = { viewModel.updateSparePart(index, description = it) },
                        label = { Text("Descripción") },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(8.dp),
                        colors = pitlaneTextFieldColors(),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.removeSparePart(index) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            TextButton(
                onClick = { viewModel.addSparePart() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar repuesto", color = ElectricCyan)
            }

            // SECCIÓN 5 — Próximo servicio
            SectionTitle("Próximo servicio")

            PitlaneTextField(
                value = state.nextServiceKm,
                onValueChange = viewModel::onNextServiceKmChange,
                label = "Próximo servicio (km)",
                placeholder = "95000",
                suffix = "km",
                keyboardType = KeyboardType.Number
            )

            // SECCIÓN 6 — Otros
            SectionTitle("Otros")

            OutlinedTextField(
                value = state.observations,
                onValueChange = viewModel::onObservationsChange,
                label = { Text("Observaciones") },
                placeholder = { Text("Notas del mecánico...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp),
                colors = pitlaneTextFieldColors(),
                maxLines = 5
            )

            PitlaneTextField(
                value = state.cost,
                onValueChange = viewModel::onCostChange,
                label = "Costo total (opcional)",
                placeholder = "12500",
                prefix = "$",
                keyboardType = KeyboardType.Decimal
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        // Botón guardar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.saveService { navController.popBackStack() }
                },
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
                        "Guardar Servicio",
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isFormValid()) Color.Black
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Componentes reutilizables

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = ElectricCyan,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = androidx.compose.ui.unit.TextUnit(
            1.5f,
            androidx.compose.ui.unit.TextUnitType.Sp
        )
    )
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val chunked = options.chunked(3)
        chunked.forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { option ->
                    val isSelected = selected == option
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelect(option) },
                        label = {
                            Text(
                                option,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = ElectricCyan,
                            borderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // Fill remaining space if row has less than 3 items
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PitlaneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    suffix: String? = null,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        suffix = suffix?.let { { Text(it, color = ElectricCyan) } },
        prefix = prefix?.let { { Text(it, color = ElectricCyan) } },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization
        ),
        colors = pitlaneTextFieldColors(),
        singleLine = true
    )
}

@Composable
private fun pitlaneTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = ElectricCyan
)