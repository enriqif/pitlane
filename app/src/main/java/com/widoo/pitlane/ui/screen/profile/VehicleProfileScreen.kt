package com.widoo.pitlane.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widoo.pitlane.data.local.VehicleCatalog
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.ui.theme.ElectricCyan
import com.widoo.pitlane.ui.theme.PitlaneTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun VehicleProfileScreen(
    viewModel: VehicleProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val editState by viewModel.editState.collectAsState()

    VehicleProfileContent(
        state = state,
        editState = editState,
        onBrandChange = viewModel::onBrandChange,
        onModelChange = viewModel::onModelChange,
        onYearChange = viewModel::onYearChange,
        onPlateChange = viewModel::onPlateChange,
        onKmChange = viewModel::onKmChange,
        onLoadForEdit = viewModel::loadVehicleForEdit,
        onSaveEdit = viewModel::saveEdit,
        onSetActive = viewModel::setActive,
        onDelete = viewModel::deleteVehicle,
        onAddNew = viewModel::addNewVehicle,
        isFormValid = viewModel.isEditFormValid()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileContent(
    state: VehicleProfileUiState,
    editState: EditVehicleUiState,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onKmChange: (String) -> Unit,
    onLoadForEdit: (VehicleEntity) -> Unit,
    onSaveEdit: (() -> Unit) -> Unit,
    onSetActive: (VehicleEntity) -> Unit,
    onDelete: (VehicleEntity) -> Unit,
    onAddNew: (String, String, Int, String, Int) -> Unit,
    isFormValid: Boolean
) {
    var showEditSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<VehicleEntity?>(null) }
    var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }

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
        Text(
            text = "Mis Vehículos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Gestioná hasta 3 vehículos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Vehicle cards
        state.vehicles.forEach { vehicle ->
            VehicleCard(
                vehicle = vehicle,
                isActive = vehicle.isActive,
                onEdit = {
                    vehicleToEdit = vehicle
                    onLoadForEdit(vehicle)
                    showEditSheet = true
                },
                onSetActive = { onSetActive(vehicle) },
                onDelete = {
                    if (!vehicle.isActive) showDeleteDialog = vehicle
                }
            )
        }

        // Add vehicle button
        if (state.canAddMore) {
            OutlinedButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    ElectricCyan.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = ElectricCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar vehículo",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "${state.vehicles.size}/3 vehículos registrados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Edit sheet
    if (showEditSheet) {
        VehicleFormSheet(
            title = "Editar vehículo",
            editState = editState,
            onBrandChange = onBrandChange,
            onModelChange = onModelChange,
            onYearChange = onYearChange,
            onPlateChange = onPlateChange,
            onKmChange = onKmChange,
            isFormValid = isFormValid,
            onSave = {
                onSaveEdit { showEditSheet = false }
            },
            onDismiss = { showEditSheet = false }
        )
    }

    // Add sheet
    if (showAddSheet) {
        var newBrand by remember { mutableStateOf("") }
        var newModel by remember { mutableStateOf("") }
        var newYear by remember { mutableStateOf("") }
        var newPlate by remember { mutableStateOf("") }
        var newKm by remember { mutableStateOf("") }

        val addFormValid = newBrand.isNotBlank() && newModel.isNotBlank() &&
                newYear.isNotBlank() && newPlate.isNotBlank() && newKm.isNotBlank()

        VehicleFormSheet(
            title = "Nuevo vehículo",
            editState = EditVehicleUiState(
                brand = newBrand,
                model = newModel,
                year = newYear,
                plate = newPlate,
                currentKm = newKm
            ),
            onBrandChange = { newBrand = it; newModel = "" },
            onModelChange = { newModel = it },
            onYearChange = { newYear = it },
            onPlateChange = { newPlate = it.uppercase() },
            onKmChange = { newKm = it },
            isFormValid = addFormValid,
            onSave = {
                onAddNew(
                    newBrand, newModel,
                    newYear.toIntOrNull() ?: 0,
                    newPlate,
                    newKm.toIntOrNull() ?: 0
                )
                showAddSheet = false
            },
            onDismiss = { showAddSheet = false }
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    "Eliminar vehículo",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "¿Seguro que querés eliminar ${vehicle.brand} ${vehicle.model}? " +
                            "Se eliminarán también todos sus servicios y cargas de combustible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(vehicle)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleEntity,
    isActive: Boolean,
    onEdit: () -> Unit,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) Modifier.border(
                    1.dp,
                    ElectricCyan.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.surfaceContainerHigh
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isActive) ElectricCyan.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (vehicle.brand == "Otra") "🏍️" else "🚗",
                    fontSize = 24.sp
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ElectricCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ACTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }
                Text(
                    text = "${vehicle.year} · ${vehicle.plate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${java.text.NumberFormat.getNumberInstance(java.util.Locale("es", "AR"))
                        .format(vehicle.currentKm)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            // Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (!isActive) {
                    IconButton(
                        onClick = onSetActive,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Activar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleFormSheet(
    title: String,
    editState: EditVehicleUiState,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onKmChange: (String) -> Unit,
    isFormValid: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var brandExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    val models = remember(editState.brand) {
        VehicleCatalog.getModels(editState.brand)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Marca
            ExposedDropdownMenuBox(
                expanded = brandExpanded,
                onExpandedChange = { brandExpanded = it }
            ) {
                OutlinedTextField(
                    value = editState.brand,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Marca") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(8.dp),
                    colors = profileTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = brandExpanded,
                    onDismissRequest = { brandExpanded = false },
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    VehicleCatalog.brands.forEach { brand ->
                        DropdownMenuItem(
                            text = { Text(brand) },
                            onClick = {
                                onBrandChange(brand)
                                brandExpanded = false
                            }
                        )
                    }
                }
            }

            // Modelo
            ExposedDropdownMenuBox(
                expanded = modelExpanded && editState.brand.isNotBlank(),
                onExpandedChange = {
                    if (editState.brand.isNotBlank()) modelExpanded = it
                }
            ) {
                OutlinedTextField(
                    value = editState.model,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Modelo") },
                    placeholder = {
                        Text(
                            if (editState.brand.isBlank()) "Primero elegí la marca"
                            else "Seleccioná el modelo"
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded)
                    },
                    enabled = editState.brand.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(8.dp),
                    colors = profileTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = modelExpanded && editState.brand.isNotBlank(),
                    onDismissRequest = { modelExpanded = false },
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                onModelChange(model)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }

            // Año + Patente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = editState.year,
                    onValueChange = { if (it.length <= 4) onYearChange(it) },
                    label = { Text("Año") },
                    placeholder = { Text("2018") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    colors = profileTextFieldColors()
                )
                OutlinedTextField(
                    value = editState.plate,
                    onValueChange = { if (it.length <= 7) onPlateChange(it) },
                    label = { Text("Patente") },
                    placeholder = { Text("AB123CD") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    colors = profileTextFieldColors()
                )
            }

            // Km
            OutlinedTextField(
                value = editState.currentKm,
                onValueChange = { if (it.length <= 7) onKmChange(it) },
                label = { Text("Kilometraje actual") },
                suffix = { Text("km", color = ElectricCyan) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                colors = profileTextFieldColors()
            )

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !editState.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (editState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Guardar",
                        fontWeight = FontWeight.Bold,
                        color = if (isFormValid) Color.Black
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = ElectricCyan
)

// Previews
@Preview(showBackground = true, showSystemUi = true, name = "Profile — Con vehículos")
@Composable
private fun VehicleProfilePreviewWithData() {
    PitlaneTheme {
        VehicleProfileContent(
            state = VehicleProfileUiState(
                vehicles = listOf(
                    VehicleEntity(
                        id = 1, brand = "Volkswagen", model = "Vento",
                        year = 2018, plate = "AB123CD",
                        currentKm = 75800, isActive = true
                    ),
                    VehicleEntity(
                        id = 2, brand = "Honda", model = "CB 250",
                        year = 2021, plate = "B123ABC",
                        currentKm = 12000, isActive = false
                    )
                ),
                activeVehicle = VehicleEntity(
                    id = 1, brand = "Volkswagen", model = "Vento",
                    year = 2018, plate = "AB123CD",
                    currentKm = 75800, isActive = true
                ),
                canAddMore = true
            ),
            editState = EditVehicleUiState(),
            onBrandChange = {}, onModelChange = {}, onYearChange = {},
            onPlateChange = {}, onKmChange = {}, onLoadForEdit = {},
            onSaveEdit = {}, onSetActive = {}, onDelete = {},
            onAddNew = { _, _, _, _, _ -> }, isFormValid = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Profile — Sin vehículos")
@Composable
private fun VehicleProfilePreviewEmpty() {
    PitlaneTheme {
        VehicleProfileContent(
            state = VehicleProfileUiState(),
            editState = EditVehicleUiState(),
            onBrandChange = {}, onModelChange = {}, onYearChange = {},
            onPlateChange = {}, onKmChange = {}, onLoadForEdit = {},
            onSaveEdit = {}, onSetActive = {}, onDelete = {},
            onAddNew = { _, _, _, _, _ -> }, isFormValid = false
        )
    }
}