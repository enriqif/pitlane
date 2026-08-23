package com.widoo.pitlane.ui.screen.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.widoo.pitlane.data.local.VehicleCatalog
import com.widoo.pitlane.ui.theme.ElectricCyan
import com.widoo.pitlane.ui.theme.LocalAccentColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            label = "onboarding_step"
        ) { step ->
            when (step) {
                0 -> WelcomeStep(onNext = { viewModel.nextStep() })
                1 -> VehicleStep(
                    uiState = uiState,
                    onBrandChange = viewModel::onBrandChange,
                    onModelChange = viewModel::onModelChange,
                    onYearChange = viewModel::onYearChange,
                    onPlateChange = viewModel::onPlateChange,
                    onKmChange = viewModel::onKmChange,
                    onNext = { if (viewModel.isVehicleFormValid()) viewModel.nextStep() },
                    onBack = { viewModel.previousStep() }
                )
                2 -> NotificationsStep(
                    notificationsEnabled = uiState.notificationsEnabled,
                    onNotificationsToggle = viewModel::onNotificationsToggle,
                    onComplete = { viewModel.saveVehicleAndComplete(onDone = onComplete) },
                    onSkip = { viewModel.saveVehicleAndComplete(onDone = onComplete) },
                    onBack = { viewModel.previousStep() },
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo + Hero
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            // Ícono con glow
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🚗", fontSize = 72.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Título con cyan glow effect
            Text(
                text = "Pitlane",
                style = MaterialTheme.typography.displayLarge,
                color = LocalAccentColor.current,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu historial de mantenimiento\nsiempre a mano",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Feature list
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Registrá servicios y repuestos",
                    "Controlá el combustible y gastos",
                    "Recibí alertas de mantenimiento"
                ).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dot con glow cyan
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Footer con botón
        Column(modifier = Modifier.padding(bottom = 48.dp)) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Comenzar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Home indicator
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleStep(
    uiState: OnboardingUiState,
    onBrandChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onKmChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var brandExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    val models = remember(uiState.brand) { VehicleCatalog.getModels(uiState.brand) }
    val isFormValid = uiState.isVehicleFormValid()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Step indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(48.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
            )
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }

        // Header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Tu vehículo",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ingresá los datos de tu auto para empezar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Marca — activo con borde cyan
        ExposedDropdownMenuBox(
            expanded = brandExpanded,
            onExpandedChange = { brandExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.brand,
                onValueChange = {},
                readOnly = true,
                label = { Text("Marca") },
                placeholder = { Text("Seleccioná la marca") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAccentColor.current,
                    unfocusedBorderColor = if (uiState.brand.isNotBlank()) LocalAccentColor.current
                    else MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = LocalAccentColor.current,
                    unfocusedLabelColor = if (uiState.brand.isNotBlank()) LocalAccentColor.current
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
            ExposedDropdownMenu(
                expanded = brandExpanded,
                onDismissRequest = { brandExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                VehicleCatalog.brands.forEach { brand ->
                    DropdownMenuItem(
                        text = { Text(brand) },
                        onClick = {
                            onBrandChange(brand)
                            onModelChange("")
                            brandExpanded = false
                        }
                    )
                }
            }
        }

        // Modelo
        ExposedDropdownMenuBox(
            expanded = modelExpanded && uiState.brand.isNotBlank(),
            onExpandedChange = { if (uiState.brand.isNotBlank()) modelExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.model,
                onValueChange = {},
                readOnly = true,
                label = { Text("Modelo") },
                placeholder = {
                    Text(
                        if (uiState.brand.isBlank()) "Primero elegí la marca"
                        else "Seleccioná el modelo"
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded)
                },
                enabled = uiState.brand.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAccentColor.current,
                    unfocusedBorderColor = if (uiState.model.isNotBlank()) LocalAccentColor.current
                    else MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = LocalAccentColor.current,
                    unfocusedLabelColor = if (uiState.model.isNotBlank()) LocalAccentColor.current
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
            ExposedDropdownMenu(
                expanded = modelExpanded && uiState.brand.isNotBlank(),
                onDismissRequest = { modelExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                value = uiState.year,
                onValueChange = { if (it.length <= 4) onYearChange(it) },
                label = { Text("Año") },
                placeholder = { Text("2018") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAccentColor.current,
                    unfocusedBorderColor = if (uiState.year.isNotBlank()) LocalAccentColor.current
                    else MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = LocalAccentColor.current,
                )
            )
            OutlinedTextField(
                value = uiState.plate,
                onValueChange = { if (it.length <= 7) onPlateChange(it) },
                label = { Text("Patente") },
                placeholder = { Text("AB123CD") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAccentColor.current,
                    unfocusedBorderColor = if (uiState.plate.isNotBlank()) LocalAccentColor.current
                    else MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = LocalAccentColor.current,
                )
            )
        }

        // Kilometraje
        OutlinedTextField(
            value = uiState.currentKm,
            onValueChange = { if (it.length <= 7) onKmChange(it) },
            label = { Text("Kilometraje actual") },
            placeholder = { Text("87450") },
            suffix = { Text("km", color = LocalAccentColor.current) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalAccentColor.current,
                unfocusedBorderColor = if (uiState.currentKm.isNotBlank()) LocalAccentColor.current
                else MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = LocalAccentColor.current,
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón Continuar
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isFormValid,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                contentColor = Color.Black,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                text = "Continuar",
                fontWeight = FontWeight.Bold,
                color = if (isFormValid) Color.Black
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Volver",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationsStep(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
) {

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) { granted ->
            // Cuando el usuario responde el diálogo del sistema, continuamos
            if (granted) {
                onComplete()
            } else {
                // Permiso denegado, igual completamos sin notificaciones
                onNotificationsToggle(false)
                onComplete()
            }
        }
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step indicator — paso 2 activo
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(48.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
            )
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(48.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.weight(1f).padding(top = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔔", fontSize = 56.sp)
            }

            Text(
                text = "Recordatorios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Activá las notificaciones para recibir alertas cuando se acerque el momento de hacer un servicio",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Toggle card
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notificaciones push",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Alertas en tiempo real",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ElectricCyan,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                }
            }
        }

        // Botones footer
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (notificationsEnabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val state = notificationPermissionState!!
                            if (state.status.isGranted) {
                                onComplete()
                            } else {
                                state.launchPermissionRequest()
                            }
                        } else {
                            onComplete()
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Ir a Pitlane",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Saltar por ahora",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Volver",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun OnboardingUiState.isVehicleFormValid(): Boolean {
    return brand.isNotBlank() && model.isNotBlank() &&
            year.isNotBlank() && plate.isNotBlank() &&
            currentKm.isNotBlank()
}