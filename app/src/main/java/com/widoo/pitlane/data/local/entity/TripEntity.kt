package com.widoo.pitlane.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

object TripStatus {
    const val PENDING = "PENDING"       // km ya aplicado, falta mostrar el snackbar
    const val ACKNOWLEDGED = "ACKNOWLEDGED"
    const val UNDONE = "UNDONE"
}

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val distanceMeters: Double,
    val previousKm: Int,
    val startedAt: Long,
    val endedAt: Long,
    val status: String = TripStatus.PENDING
)
