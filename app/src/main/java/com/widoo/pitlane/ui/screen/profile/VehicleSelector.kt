package com.widoo.pitlane.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.widoo.pitlane.data.local.entity.VehicleEntity
import com.widoo.pitlane.ui.screen.service.pitlaneTextFieldColors
import com.widoo.pitlane.ui.theme.ElectricCyan
import com.widoo.pitlane.ui.theme.LocalAccentColor
import kotlin.collections.forEach


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelector(
    vehicles: List<VehicleEntity>,
    selectedVehicleName: String,
    onVehicleSelected: (VehicleEntity) -> Unit
) {
    // Si solo hay 1 vehículo no mostramos el selector
    if (vehicles.size <= 1) return

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedVehicleName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Vehículo") },
            leadingIcon = {
                Icon(
                    Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = LocalAccentColor.current
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(8.dp),
            colors = pitlaneTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            vehicles.forEach { vehicle ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${vehicle.brand} ${vehicle.model}",
                                fontWeight = FontWeight.Medium
                            )
                            if (vehicle.isActive) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ElectricCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "activo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LocalAccentColor.current,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }
                        }
                    },
                    leadingIcon = {
                        Text("🚗", fontSize = 16.sp)
                    },
                    onClick = {
                        onVehicleSelected(vehicle)
                        expanded = false
                    }
                )
            }
        }
    }
}