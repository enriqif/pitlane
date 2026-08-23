package com.widoo.pitlane.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val model: String,
    val year: Int,
    val plate: String,
    val currentKm: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    // Campos para la futura sincronización con la API
    val remoteId: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)