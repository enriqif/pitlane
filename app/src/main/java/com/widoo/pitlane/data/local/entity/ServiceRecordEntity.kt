package com.widoo.pitlane.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "service_records")
data class ServiceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long = 0,
    val date: Long,
    val km: Int,
    val serviceType: String,
    val oilBrand: String = "",
    val oilType: String = "",
    val oilViscosity: String = "",
    val filtersChanged: String = "",  // JSON: ["aceite","aire"]
    val spareParts: String = "",      // JSON: [{"marca":"x","desc":"y"}]
    val observations: String = "",
    val nextServiceKm: Int = 0,
    val nextServiceDate: Long = 0,
    val cost: Double = 0.0,
    // Campos para la futura sincronización con la API
    val remoteId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)