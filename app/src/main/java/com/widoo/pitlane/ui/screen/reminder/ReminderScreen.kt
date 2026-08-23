package com.widoo.pitlane.ui.screen.reminder

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.widoo.pitlane.data.local.entity.ReminderEntity
import com.widoo.pitlane.ui.theme.ElectricCyan
import com.widoo.pitlane.ui.theme.LocalAccentColor
import com.widoo.pitlane.ui.theme.PitlaneTheme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReminderScreen(viewModel: ReminderViewModel = koinViewModel()) {
    val pendingReminders by viewModel.pendingReminders.collectAsState()
    val allReminders by viewModel.completedReminders.collectAsState()
    val completedReminders = allReminders.filter { it.isCompleted }
    val addState by viewModel.addState.collectAsState()

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    ReminderScreenContent(
        pendingReminders = pendingReminders,
        completedReminders = completedReminders,
        isNotificationGranted = notificationPermission?.status?.isGranted ?: true,
        onNotificationToggle = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermission?.launchPermissionRequest()
            }
        },
        onMarkCompleted = viewModel::markAsCompleted,
        onDelete = viewModel::delete,
        addState = addState,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDateChange = viewModel::onDateChange,
        onNotificationsToggle = viewModel::onNotificationsToggle,
        isFormValid = viewModel.isFormValid(),
        onSave = viewModel::saveReminder
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreenContent(
    pendingReminders: List<ReminderEntity>,
    completedReminders: List<ReminderEntity>,
    isNotificationGranted: Boolean = true,
    onNotificationToggle: () -> Unit = {},
    onMarkCompleted: (ReminderEntity) -> Unit,
    onDelete: (ReminderEntity) -> Unit,
    addState: AddReminderUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    isFormValid: Boolean,
    onSave: (() -> Unit) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mis Recordatorios",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Mantené tu vehículo al día con alertas automáticas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notifications toggle
            item {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = LocalAccentColor.current,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Notificaciones Push",
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
                        }
                        Switch(
                            checked = isNotificationGranted,
                            onCheckedChange = { onNotificationToggle() },
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

            // Pending section header
            if (pendingReminders.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PENDIENTES",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalAccentColor.current,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = "${pendingReminders.size} ALERTAS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 3.dp
                                )
                            )
                        }
                    }
                }

                items(pendingReminders, key = { it.id }) { reminder ->
                    PendingReminderCard(
                        reminder = reminder,
                        onMarkCompleted = { onMarkCompleted(reminder) },
                        onDelete = { onDelete(reminder) }
                    )
                }
            }

            // Completed section header
            if (completedReminders.isNotEmpty()) {
                item {
                    Text(
                        text = "COMPLETADOS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(completedReminders, key = { it.id }) { reminder ->
                    CompletedReminderCard(
                        reminder = reminder,
                        onDelete = { onDelete(reminder) }
                    )
                }
            }

            // Empty state
            if (pendingReminders.isEmpty() && completedReminders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🔔", fontSize = 48.sp)
                            Text(
                                text = "Sin recordatorios",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tocá + para crear el primero",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Suggestions
            item {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SUGERENCIAS COMUNES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Aceite", "Frenos", "Cubiertas", "Batería").forEach { suggestion ->
                            SuggestionChip(
                                onClick = { onTitleChange(suggestion) },
                                label = {
                                    Text(
                                        suggestion,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
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
            icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            text = {
                Text(
                    "Nuevo recordatorio",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        )
    }

    if (showAddSheet) {
        AddReminderSheet(
            state = addState,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onDateChange = onDateChange,
            onNotificationsToggle = onNotificationsToggle,
            isFormValid = isFormValid,
            isLoading = addState.isLoading,
            onSave = { onSave { showAddSheet = false } },
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun PendingReminderCard(
    reminder: ReminderEntity,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(reminder.triggerDate))
    val daysUntil = ((reminder.triggerDate - System.currentTimeMillis()) /
            (1000 * 60 * 60 * 24)).toInt()
    val isOverdue = daysUntil < 0
    val accentColor = if (isOverdue) MaterialTheme.colorScheme.error else LocalAccentColor.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Min)
                    .background(accentColor)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isOverdue) Icons.Filled.Warning else Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (reminder.description.isNotBlank()) {
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vence el $date",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (isOverdue) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Vencido",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else if (daysUntil <= 30) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "En $daysUntil días",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onMarkCompleted, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Completar",
                            tint = LocalAccentColor.current,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedReminderCard(
    reminder: ReminderEntity,
    onDelete: () -> Unit
) {
    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(Date(reminder.triggerDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough
                )
                Text(
                    text = "Finalizado el $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onDelete) {
                Text(
                    text = "ELIMINAR",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderSheet(
    state: AddReminderUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    isFormValid: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.triggerDate
    )
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nuevo recordatorio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Título") },
                placeholder = { Text("Ej: Vencimiento seguro, VTV...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAccentColor.current,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = LocalAccentColor.current,
                    cursorColor = LocalAccentColor.current
                )
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Notas adicionales...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAccentColor.current,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = LocalAccentColor.current,
                    cursorColor = LocalAccentColor.current
                )
            )

            OutlinedTextField(
                value = dateFormatter.format(Date(state.triggerDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha del recordatorio") },
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Cambiar", color = LocalAccentColor.current)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalAccentColor.current,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = LocalAccentColor.current,
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
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
                    Column {
                        Text(
                            text = "Notificación push",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Recibí un aviso en la fecha indicada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ElectricCyan
                        )
                    )
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                        "Guardar recordatorio",
                        fontWeight = FontWeight.Bold,
                        color = if (isFormValid) Color.Black
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateChange(it) }
                    showDatePicker = false
                }) { Text("OK", color = LocalAccentColor.current) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = ElectricCyan,
                    selectedDayContentColor = Color.Black,
                    todayDateBorderColor = ElectricCyan
                )
            )
        }
    }
}

// Previews
@Preview(showBackground = true, showSystemUi = true, name = "Reminders — Con datos")
@Composable
private fun ReminderScreenPreviewWithData() {
    PitlaneTheme {
        ReminderScreenContent(
            pendingReminders = listOf(
                ReminderEntity(
                    id = 1, vehicleId = 1,
                    title = "Vencimiento Seguro",
                    description = "",
                    triggerDate = System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000,
                    isCompleted = false
                ),
                ReminderEntity(
                    id = 2, vehicleId = 1,
                    title = "VTV",
                    description = "Turno solicitado",
                    triggerDate = System.currentTimeMillis() + 25L * 24 * 60 * 60 * 1000,
                    isCompleted = false
                )
            ),
            completedReminders = listOf(
                ReminderEntity(
                    id = 3, vehicleId = 1,
                    title = "Cambio Filtro Aire",
                    description = "",
                    triggerDate = System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000,
                    isCompleted = true
                )
            ),
            onMarkCompleted = {},
            onDelete = {},
            addState = AddReminderUiState(),
            onTitleChange = {},
            onDescriptionChange = {},
            onDateChange = {},
            onNotificationsToggle = {},
            isFormValid = false,
            onSave = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Reminders — Sin datos")
@Composable
private fun ReminderScreenPreviewEmpty() {
    PitlaneTheme {
        ReminderScreenContent(
            pendingReminders = emptyList(),
            completedReminders = emptyList(),
            onMarkCompleted = {},
            onDelete = {},
            addState = AddReminderUiState(),
            onTitleChange = {},
            onDescriptionChange = {},
            onDateChange = {},
            onNotificationsToggle = {},
            isFormValid = false,
            onSave = {}
        )
    }
}