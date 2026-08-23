package com.widoo.pitlane.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

object TripStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val DISCARDED = "DISCARDED"
}

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val distanceMeters: Double,
    val startedAt: Long,
    val endedAt: Long,
    val status: String = TripStatus.PENDING
)
