package com.widoo.pitlane.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "fuel_logs")
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val date: Long,
    val km: Int,
    val liters: Double,
    val pricePerLiter: Double,
    val totalCost: Double,
    val station: String = "",
    // Campos para la futura sincronización con la API
    val remoteId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)